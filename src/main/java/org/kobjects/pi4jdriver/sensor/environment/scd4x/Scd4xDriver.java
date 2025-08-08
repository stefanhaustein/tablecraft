package org.kobjects.pi4jdriver.sensor.environment.scd4x;

import com.pi4j.io.i2c.I2C;

public class Scd4xDriver {

    public static final int I2C_ADDRESS = 0x62;
    private byte[] buf = new byte[64];

    private final I2C i2c;

    public Scd4xDriver(I2C i2c) {
        this.i2c = i2c;
    }

    /** Requests a single shot measurement */
    public void measureSingleShot() {
        sendCommand(0x29d);
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendCommand(0xec05);
        try {
            Thread.sleep(1);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(readValue());
        System.out.println(readValue());
        System.out.println(readValue());

    }

    public void sendCommand(int cmd, int... args) {
        int idx = 0;
        buf[idx++] = (byte) (cmd >>> 8);
        buf[idx++] = (byte) cmd;

        i2c.write(buf, 0, 2 + args.length * 3);
    }

    public int readValue() {
        int hi = i2c.read();
        int lo = i2c.read();
        int crc = i2c.read();
        return (hi << 8) | lo;
    }

}
