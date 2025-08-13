package org.kobjects.pi4jdriver.sensor.scd4x;

import com.pi4j.io.i2c.I2C;

/** Pi4J-based driver for SCD4X co2 (+ temperature and humidity) sensors */
public class Scd4xDriver {

    /** The I2C address of the device (needed for constructing an I2C instance) */
    public static final int I2C_ADDRESS = 0x62;
    private final byte[] buf = new byte[64];
    private long busyUntil;

    private final I2C i2c;

    /**
     * Creates a driver instance, connected via the given I2C instance. Note that the device needs to be set to
     * I2C_ADDRESS when building the I2C instance.
     */
    public Scd4xDriver(I2C i2c) {
        this.i2c = i2c;
    }

    // Basic commands; Chapter 3.5

    /** Starts periodic measurement. This is already the default state on powerup. */
    public void startPeriodicMeasurement() {
        sendCommand(0x21b1, 0);
    }

    /**
     * Read a measurement. Note that no further measurements will be available until the device actually
     * performs another measurement.
     */
    public Measurement readMeasurement() {

        materializeDelay();

        while (!getDataReadyStatus()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendCommand(0xec05, 1);
        i2c.read(buf, 0, 3*3);
        int co2 = ((buf[0] & 0xff) <<8) | (buf[1] & 0xff);
        int raw_temperature = ((buf[3] & 0xff) <<8) | (buf[4] & 0xff);
        int raw_humidity = ((buf[6] & 0xff) <<8) | (buf[7] & 0xff);

        return new Measurement(
                co2,
                -45 + 175.0f * raw_temperature / 65535.0f,
                100.0f * raw_humidity / 65535.0f);
    }

    public void stopPeriodicMeasurement() {
        sendCommand(0x3f86, 500);
    }

    // On-chip output signal compensation; chapter 3.6

    /**
     * Set the temperature offset in °C.
     * <p>
     * Correctly setting the temperature offset is required for
     * accurate humidity and temperature readings. This offset doesn't affect the sensor's
     * CO₂ accuracy.
     * <p>
     * Several factors can influence the correct temperature offset, including:
     * <ul>
     * <li>The SCD4x's measurement mode
     * <li>Heat from nearby components
     * <li>Ambient temperature
     * <li>Airflow
     * </ul>
     * Because of this, the correct temperature offset should be determined
     * under typical operating conditions. This means the device should be in its normal
     * operating mode and have reached thermal equilibrium.
     * <p>
     * By default, the temperature offset is set to 4° C. To permanently save a new offset
     * value, the persistSetting command is required.
     */
    public void setTemperatureOffset(double offsetC) {
        sendCommand(0x241d, 1, (int) (offsetC * 65536.0 / 175.0));
    }

    /** Returns the current temperature offset in °C. */
    public double getTemperatureOffset() {
        sendCommand(0x2318, 1);
        return readValue() * 175.0 / 65536.0;
    }

    /**
     * Set the sensor's altitude in meter above sea level.
     * <p>
     * The sensor's altitude must be read and written only while the SCD4x is in idle mode.
     * Typically, this setting is configured just once after the device has been installed.
     * To permanently save the altitude to the EEPROM, the persist setting command must be issued.
     * By default, the sensor altitude is set to 0 meters above sea level.
     */
    public void setSensorAltitude(int altitudeMasl) {
        sendCommand(0x2427, 1, altitudeMasl);
    }

    public int getSensorAltitude() {
        sendCommand(0x2322, 1);
        return readValue();
    }

    public void setAmbientPressure(int pressurePa) {
        sendCommand(0xe000, 1, pressurePa / 100);
    }

    // Chapter 3.7: Field Calibration

    public int performForcedRecalibration(int referenceCo2ppm) {
        sendCommand(0x362f, 400, referenceCo2ppm);
        int result = readValue();
        if (result == 0xffff) {
            throw new IllegalStateException("Recalibration has failed.");
        }
        return result - 0x8000;
    }

    public void setAutomaticSelfCalibrationEnabled(boolean enabled) {
        sendCommand(0x2416, 1, enabled ? 1 : 0);
    }

    public boolean getAutomaticSelfCalibrationEnabled() {
        sendCommand(0x2313, 1);
        return readValue() != 0;
    }

    // Chapter 3.8: Low Power Operation

    public void startLowPowerPeriodicMeasurement() {
        sendCommand(0x21ac, 0);
    }

    /** Returns true if a measurement is available; false otherwise. */
    public boolean getDataReadyStatus() {
        sendCommand(0xe4b8, 1);
        int readyState = readValue();
        return (readyState & 0b011111111111) != 0;
    }

    // Chapter 3.9: Advanced Featurs

    public void persistSettings() {
        sendCommand(0x3615, 800);
    }

    /** Returns the 48 bit serial number of the device as a long. */
    public long gerSerialNumber() {
        sendCommand(0x3682, 1);
        materializeDelay();
        i2c.read(buf, 0, 3*3);
        return ((buf[0] & 0xffL) << 40L)
                | ((buf[1] & 0xffL) << 32)
                | ((buf[3] & 0xffL) << 24)
                | ((buf[4] & 0xffL) << 16)
                | ((buf[6] & 0xffL) << 8)
                | (buf[7] & 0xffL);
    }

    /**
     * Returns a value other than 0 if an issue was detected.
     * Note that this command takes a very long time (10s)
     */
    public int performSelfTest() {
        sendCommand(0x3639, 10000);
        return readValue();
    }

    public void performFactoryReset() {
        sendCommand(0x3632, 1200);
    }

    public void reInit() {
        sendCommand(0x3646, 30);
    }

    // Chapter 3.10: Low power single shot (SCD41)

    /** Requests a single shot measurement; only available for the SCD41 sensor. */
    public void measureSingleShot() {
        sendCommand(0x29d, 5000);
    }

    /**
     * Requests a single shot humidity and temperature measurement;
     * only available for the SCD41 sensor.
     */
    public void measureSingleShotRhtOnly() {
        sendCommand(0x2196, 50);
    }

    public void powerDown() {
        sendCommand(0x36e0, 1);
    }

    public void wakeUp() {
        sendCommand(0x36f6, 1);
    }

    // Internal helpers

    private byte crc(byte[] data, int offset, int count) {
        byte crc = (byte) 0xff;
        for (int index = offset; index < offset + count; index++) {
            crc ^= data[index];
            for (int crcBit = 8; crcBit > 0; --crcBit) {
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc << 1) ^ 0x31);
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc;
    }


    /**
     * Sends the given command to the chip after materializing the delay implied
     * by the previous command and keeps track of the delay implied by this command
     * (from the timeMs parameter) in busyUntil.
     */
    private void sendCommand(int cmdCode, int timeMs, int... args) {
        materializeDelay();

        int idx = 0;
        buf[idx++] = (byte) (cmdCode >>> 8);
        buf[idx++] = (byte) cmdCode;

        for (int i = 0; i < args.length; i++) {
            int p0 = idx;
            buf[idx++] = (byte) (args[i] >>> 8);
            buf[idx++] = (byte) args[i];
            buf[idx++] = crc(buf, p0, 2);
        }

        i2c.write(buf, 0, idx);

        // Assume at least 1ms and add one ms as we don't know how much time is remaining to the next millisecond.
        busyUntil = System.currentTimeMillis() + Math.max(1, timeMs) + 1;
    }

    public int readValue() {
        materializeDelay();
        i2c.read(buf, 0, 3);
        return ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
    }

    private void materializeDelay() {
        while (true) {
            long time = System.currentTimeMillis();
            if (time >= busyUntil) {
                break;
            }
            try {
                Thread.sleep(Math.max(1, busyUntil - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A measurement record containing the measured values returned form readMeasurement()
     */
    public static class Measurement {
        private final int co2;
        private final float temperature;
        private final float humidity;

        public Measurement(int co2, float temperature, float humidity) {
            this.co2 = co2;
            this.temperature = temperature;
            this.humidity = humidity;
        }

        @Override
        public String toString() {
            return "co2 = " + co2 + " ppm; temperature = " + temperature + " °C; humidity = " + humidity + " %";
        }

        /** Measured co2 concentration in ppm */
        public int getCo2() {
            return co2;
        }

        /** Measured temperature in C */
        public float getTemperature() {
            return temperature;
        }

        /** Measured relative humidity in % */
        public float getHumidity() {
            return humidity;
        }
    }
}
