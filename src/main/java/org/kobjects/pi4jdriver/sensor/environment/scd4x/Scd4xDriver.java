package org.kobjects.pi4jdriver.sensor.environment.scd4x;

import com.pi4j.io.i2c.I2C;

public class Scd4xDriver {

    public static final int I2C_ADDRESS = 0x62;
    private byte[] buf = new byte[64];
    private long busyUntil;

    private final I2C i2c;

    public Scd4xDriver(I2C i2c) {
        this.i2c = i2c;
    }

    public void startPeriodicMeasurement() {
        sendCommand(0x21b1, 0);
    }

    public Measurement readMeasurement() {
        sendCommand(0xec05, 1);

        materializeDelay();

        i2c.read(buf, 0, 3*3);
        int co2 = ((buf[0] & 0xff) <<8) | (buf[1] & 0xff);
        int raw_temperature = ((buf[3] & 0xff) <<8) | (buf[4] & 0xff);
        int raw_humidity = ((buf[6] & 0xff) <<8) | (buf[7] & 0xff);

        return new Measurement(co2, -45 + 175.0 * raw_temperature / 65535.0, 100.0 * raw_humidity / 65535.0);
    }

    public void stopPeriodicMeasurement() {
        sendCommand(0x3f86, 500);
    }

    public boolean getDataReadyStatus() {
        sendCommand(0xe4b8, 1);
        int readyState = readValue();
        System.out.println("state: " + readyState + " masked: "+(readyState & 0b011111111111));
        return (readValue() & 0b011111111111) != 0;
    }

    public void reInit() {
        sendCommand(0x3646, 30);
    }

    /** Requests a single shot measurement */
    public void measureSingleShot() {
        sendCommand(0x29d, 5000);
    }

    public void measureSingleShotRhtOnly() {
        sendCommand(0x2196, 50);
    }

    public void sendCommand(int cmd, int time, int... args) {
        materializeDelay();

        int idx = 0;
        buf[idx++] = (byte) (cmd >>> 8);
        buf[idx++] = (byte) cmd;

        i2c.write(buf, 0, 2 + args.length * 3);

        busyUntil = System.currentTimeMillis() + time;
    }

    public int readValue() {
        materializeDelay();
        i2c.read(buf, 0, 3);
        return ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
    }

    private void materializeDelay() {
        while (true) {
            long time = System.currentTimeMillis();
            if (time > busyUntil) {  // > for safety if current milli was nearly over already.
                break;
            }
            try {
                Thread.sleep(Math.max(1, busyUntil - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Measurement {
        int co2;
        double temperature;
        double humidity;

        public Measurement(int co2,  double temperature, double humidity) {
            this.co2 = co2;
            this.temperature = temperature;
            this.humidity = humidity;
        }

        @Override
        public String toString() {
            return "co2: " + co2 + "ppm; t: " + temperature + " humidity: " + humidity + "%";
        }
    }

}
