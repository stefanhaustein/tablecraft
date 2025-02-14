package freenove;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CConfigBuilder;
import com.pi4j.io.i2c.I2CProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

class IIC {
    protected String dev;
    protected int handle;
    protected int slave;
    protected byte[] out;
    protected boolean transmitting;

    private Context pi4j;
    I2CConfigBuilder i2cConfigBuilder;
    I2CProvider i2CProvider;
    I2CConfig i2cConfig;
    I2C i2c;
    int bus = 1;

    public IIC(int bus) {
        this.bus = bus;
        constructor();
    }

    public IIC(String s) {
        String b = s.split("i2c-")[1];
        try {
            this.bus = Integer.parseInt(b);
        } catch (Exception e) {
            this.bus = 1;
        }
        constructor();
    }

    private void constructor() {
        pi4j = Pi4J.newAutoContext();
        i2CProvider = pi4j.provider("linuxfs-i2c");
        i2cConfigBuilder = I2C.newConfigBuilder(pi4j).bus(bus);
    }

    public void beginTransmission(int address) {
        if (i2cConfig == null) {
            i2cConfig = i2cConfigBuilder.device(address).build();
        }

        if (i2c == null) {
            i2c = i2CProvider.create(i2cConfig);
        }
    }

    public void write(int b) {
        i2c.write(b);
    }

    public byte read() {
        return i2c.readByte();
    }

    public byte[] read(int size) {
        return i2c.readByteBuffer(size).array();
    }

    public void endTransmission() {
        i2c.close();
    }

    public static String[] list() {
        ArrayList<String> devs = new ArrayList<String>();
        File dir = new File("/dev");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("i2c-")) {
                    devs.add(file.getName());
                }
            }
        }
        String[] tmp = devs.toArray(new String[devs.size()]);
        Arrays.sort(tmp);
        return tmp;
    }
}
