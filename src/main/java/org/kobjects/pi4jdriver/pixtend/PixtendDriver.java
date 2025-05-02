package org.kobjects.pi4jdriver.pixtend;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiMode;

import java.io.Closeable;


public class PixtendDriver implements Closeable {
    public final Model model;
    private final Context pi4J;

    private final Spi spi;
    private final DigitalOutput pin24dout;

    private final byte[] spiIn;
    private final byte[] spiOut;
    private final GpioMode[] gpioModes = {
            GpioMode.DIGITAL_INPUT, GpioMode.DIGITAL_INPUT,
            GpioMode.DIGITAL_INPUT, GpioMode.DIGITAL_INPUT};

    public PixtendDriver(Model model, Context pi4J) {
        this.pi4J = pi4J;
        this.model = model;

        this.spiIn = new byte[model.bufferSize];
        this.spiOut = new byte[model.bufferSize];

        spiOut[0] = (byte) model.modelOut;

        // Enable Pixtend SPI communication by enabling bwm pin 24
        pin24dout = pi4J.create(DigitalOutputConfig.newBuilder(pi4J).address(24).build());
        pin24dout.setState(true);

        SpiConfig spiConfig = Spi.newConfigBuilder(pi4J).provider("linuxfs-spi")
                .mode(SpiMode.MODE_0)
                .chipSelect(SpiChipSelect.CS_0)
                .address(0)
                .baud(700_000)
                .build();

        spi = pi4J.create(spiConfig);

        spi.open();
    }

    /** Closes SPI and shuts down pin 24 */
    @Override
    public void close() {
        spi.close();
        pi4J.shutdown(pin24dout.id());
    }

    /** Sends the current state to the device and receives an update */
    public void syncState() {

        // Header checksum
        int headerChecksum = crc16(spiOut, 0, 7);
        System.out.println("Header crc: " + headerChecksum);
        setWord(7, headerChecksum);

        //Calculate CRC16 Data Transmit Checksum
        int dataChecksum = crc16(spiOut, 9, spiOut.length - 2);
        setWord(spiOut.length - 2, dataChecksum);

        int result = spi.transfer(spiOut, spiIn, spiOut.length);

        System.out.println("Received: new");
        for (int i = 0; i < spiIn.length; i++) {
            System.out.print("" + Character.forDigit((spiIn[i] & 255) / 16, 16) + Character.forDigit((spiIn[i] & 255) % 16, 16) + ' ');
        }
        System.out.println();

        if (result != spiOut.length) {
            throw new IllegalStateException("Expected " + spiOut.length + " bytes; got: " + result);
        }

        int receivedHeaderChecksum = getWord(7);
        int calculatedHeaderChecksum = crc16(spiIn, 0, 7);
        if (receivedHeaderChecksum != calculatedHeaderChecksum) {
            throw new IllegalStateException("Received header checksum " + receivedHeaderChecksum + " != calculated checksum " + calculatedHeaderChecksum);
        }

        int errorCode = getNibble(3, 1);
        switch (errorCode) {
            case 0b0010: throw new IllegalStateException("Data CRC Error");
            case 0b0011: throw new IllegalStateException("Data block too short");
            case 0b0100: throw new IllegalStateException("PiXtend Model mismatch");
            case 0b0101: throw new IllegalStateException("Header CRC Error");
            case 0b0110: throw new IllegalStateException("SPI frequency too high");
        }


    }

    /** Returns the 10V-range voltage for analog in 0..3 and a mA value for analog in 4 and 5 */
    public double getAnalogIn(int index) {
        return (index > 4)
            ? getRawAnalogIn(index) * 0.020158400229358
            : getAnalogIn(index, false);
    }

    /** Returns the 10/5V range voltage for analog in 0..3. Analog 4/5 are not supported in this call. */
    public double getAnalogIn(int index, boolean limitedTo5v) {
        if (index > 4) {
            throw new IllegalArgumentException("limitedTo5v argument not supported for analog in 4/5") ;
        }
        return getRawAnalogIn(index) * (limitedTo5v ? 5.0 : 10.0) / 1024.0;
    }

    public boolean getDigitalIn(int index) {
        if (index < 0 || index >= model.digitalInCount) {
            throw new IllegalArgumentException("Digital input index " + index + " out of range 0.." + (model.digitalInCount - 1));
        }
        return getBit(model.digitalInOffset, index);
    }

    public boolean getGpioIn(int index) {
        checkRange(index, model.gpioCount, "GPIO input");
        if (gpioModes[index] != GpioMode.DIGITAL_INPUT) {
            throw new IllegalStateException("GpioMode " + index + " is set to " + gpioModes[index]);
        }
        return getBit(model.gpioInOffset, index);
    }

    public double getTemperature(int index) {
        checkRange(index, model.tempHumidCount, "Temperature");
        int rawValue = getWord(model.tempHumidOffset + index * 4);
        return switch (gpioModes[index]) {
            case DHT11 -> rawValue / 256.0;
            case DHT22 -> rawValue / 10.0;
            default -> throw new IllegalStateException("GPIO " + index + " not configured for DHT11/22");
        };
    }

    public double getHumidity(int index) {
        checkRange(index, model.tempHumidCount, "Humidity");
        int rawValue = getWord(model.tempHumidOffset + index * 4 + 2);
        return switch (gpioModes[index]) {
            case DHT11 -> rawValue / 256.0;
            case DHT22 -> rawValue / 10.0;
            default -> throw new IllegalStateException("GPIO " + index + " not configured for DHT11/22");
        };
    }

    /** Returns the "raw" analog input value in a 10 bit range [0..1023]. */
    public int getRawAnalogIn(int index) {
        checkRange(index, model.analogInCount, "Analog input");
        return getWord(model.analogInOffset + 2 * index);
    }

    public void setDigitalOut(int index, boolean value) {
        checkRange(index, model.digitalInCount, "Digital output");
        setBit(model.digitalOutOffset, index, value);
    }

    public void setGpioMode(int index, GpioMode mode) {
        checkRange(index, model.gpioCount, "GPIO mode");
        switch (mode) {
            case DHT11:
            case DHT22:
                setBit(model.gpioCtrlOffset, index, false);
                setBit(model.gpioCtrlOffset, index + 4, true);
                break;
            case DIGITAL_INPUT:
                setBit(model.gpioCtrlOffset, index, false);
                setBit(model.gpioCtrlOffset, index + 4, false);
                break;
            case DIGITAL_OUTPUT:
                setBit(model.gpioCtrlOffset, index, true);
                setBit(model.gpioCtrlOffset, index + 4, false);
        }
        gpioModes[index] = mode;
    }

    public void setGpioOut(int index, boolean value) {
        checkRange(index, model.gpioCount, "GPIO output");
        if (gpioModes[index] != GpioMode.DIGITAL_OUTPUT) {
            throw new IllegalStateException("GpioMode " + index + " is set to " + gpioModes[index]);
        }
        setBit(model.gpioOutOffset, index, value);
    }

    public void setRelay(int index, boolean value) {
        checkRange(index, model.relayOutCount, "Relay");
        setBit(model.relayOutOffset, index, value);
    }

    // Private helpers

    private static void checkRange(int index, int count, String name) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException(name + " index " + index + " out of range 0.." + (count - 1));
        }
    }

    private static int crc16(byte[] data, int from, int toExclusive) {
        int crc = 0xFFFF;
        for (int i = from; i < toExclusive; i++) {
            crc = crc16(crc, data[i]);
        }
        return crc;
    }

    private static int crc16(int crc, byte data) {
        crc ^= (data & 0xFF);
        for (int i = 0; i < 8; i++) {
            crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : (crc >>> 1);
        }
        return crc;
    }

    private boolean getBit(int baseAddress, int bitIndex) {
        int address = baseAddress + bitIndex / 8;
        return (spiIn[address] & (1 << (bitIndex % 8))) != 0;
    }

    private int getNibble(int baseAddress, int index) {
        int address = baseAddress + index / 2;
        return (spiIn[address] >>> ((index % 2) * 4)) & 0x0f;
    }

    private int getWord(int address) {
        return (spiIn[address] & 0xff) | ((spiIn[address + 1] & 0xff) << 8);
    }

    private void setBit(int baseAddress, int bitIndex, boolean value) {
        int address = baseAddress + bitIndex / 8;
        int mask = ~(1 << (bitIndex % 8));
        int bitValue = value ? 1 << bitIndex : 0;
        spiOut[address] = (byte) ((spiOut[address] & mask) | bitValue);
    }

    private void setNibble(int baseAddress, int index, int value) {
        int address = baseAddress + index / 2;
        int oldValue = spiIn[address] & 255;
        spiOut[address] = (byte) ((index & 1) == 0
                ? ((oldValue & 0xf0) | (value & 0x0f))
                : ((oldValue & 0x0f) | ((value & 0x0f) << 4)));
    }

    private void setWord(int address, int word) {
        spiOut[address] = (byte) word;
        spiOut[address + 1] = (byte) (word >>> 8);
    }

}
