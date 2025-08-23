package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.i2c.I2C;

public class Aip31068lConnection extends AbstractConnection {
    private final I2C i2c;
    private byte[] buf = new byte[2];

    Aip31068lConnection(I2C i2c) {
        this.i2c = i2c;
    }

    @Override
    protected void setBacklight(boolean on) {
        // Unsupported
    }

    @Override
    protected void sendValue(boolean rs, int value) {
        buf[0] = (byte) (rs ? 0x0100_0000 : 0);
        buf[1] = (byte) value;
        i2c.write(buf, 0, 2);
    }
}
