package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.i2c.I2C;

/** https://www.orientdisplay.com/wp-content/uploads/2022/08/AIP31068L.pdf */
public class Aip31068Connection extends AbstractConnection {
    private final I2C i2c;
    private byte[] buf = new byte[2];

    public Aip31068Connection(I2C i2c) {
        this.i2c = i2c;
        // Bring the display into a well-defined 4 bit state
    }

    @Override
    protected boolean is8Bit() {
        return true;
    }

    @Override
    protected void setBacklight(boolean on) {
        // Unsupported
    }

    @Override
    protected void sendValue(Mode mode, int value) {
        i2c.write((byte) (mode == Mode.DATA ? 0x0100_0000 : 0),  (byte) value);
        setDelayMicros(5000);
    }
}
