package org.kobjects.pi4jdriver.pixtend;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;


public class Pixtend {
    private final Context pi4J;
    private final Spi spi;
    private final DigitalOutput pin24dout;

    private final byte[] spiIn;
    private final byte[] spiOut;

    public Pixtend(Context pi4J, Model model) {
        this.pi4J = pi4J;

        this.spiIn = new byte[67];
        this.spiOut = new byte[67];

        // Enable Pixtend SPI communication by enabling bwm pin 24
        pin24dout = pi4J.create(DigitalOutputConfig.newBuilder(pi4J).address(24).build());
        pin24dout.setState(true);

        SpiConfig spiConfig = Spi.newConfigBuilder(pi4J).provider("linuxfs-spi")
                // .mode(SpiMode.MODE_0)
                //   .chipSelect(SpiChipSelect.CS_0)
                .address(0)
                .baud(700_000)
                .build();

        spi = pi4J.create(spiConfig);



        spi.open();


    }

    /** Sends the current state to the device and receives an update */
    public void sync() {
        int headerCrc = 0xFFFF;

        for (int i = 0; i < 7; i++) {
            headerCrc = crc16(headerCrc, spiOut[i]);
        }

        spiOut[7] = (byte) headerCrc;  //CRC Low Byte
        spiOut[8] = (byte) (headerCrc >>> 8); //CRC High Byte

        //Calculate CRC16 Data Transmit Checksum
        var dataCrc = 0xFFFF;
        for (int i = 0; i < 65; i++) {
            dataCrc = crc16(dataCrc, spiOut[i]);
        }

        spiOut[65] = (byte) dataCrc; //CRC Low Byte
        spiOut[66] = (byte) (dataCrc >>> 8); //CRC High Byte


        System.out.println("Spi transfer result: "  + spi.transfer(spiOut, spiIn, 67));


        System.out.println("Received: new");
        for (int i = 0; i < spiIn.length; i++) {
            System.out.print("" + Character.forDigit((spiIn[i] & 255) / 16, 16) + Character.forDigit((spiIn[i] & 255) % 16, 16) + ' ');
        }
        System.out.println();
    }



    private static int crc16(int crc, byte data) {
        crc ^= (data & 0xFF);
        for (int i = 0; i < 8; i++) {
            crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xa001 : (crc >>> 1);
        }
        return crc;
    }


    // Only model S is supported at this time.
    public enum Model {
        S
    }
}
