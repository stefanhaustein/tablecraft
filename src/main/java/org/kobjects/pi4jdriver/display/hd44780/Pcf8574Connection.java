package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.i2c.I2C;

public class Pcf8574Connection extends AbstractConnection {
    private static final int FLAG_REGISTER_SELECT = 0b0000_0001;  // Set when writing text/data (opposed to command)
    private static final int FLAG_READ_WRITE =      0b0000_0010;
    private static final int FLAG_ENABLE =          0b0000_0100;
    private static final int FLAG_BACKLIGHT =       0b0000_1000;

    private final I2C i2c;

    private boolean backlightEnabled;

    public Pcf8574Connection(I2C i2C) {
        this.i2c = i2C;
    }

    @Override
    protected boolean is8Bit() {
        return false;
    }

    @Override
    public void setBacklight(boolean on) {
        this.backlightEnabled = on;
    }

    @Override
    public void sendValue(Mode mode, int value) {
        boolean registerSelect = mode == Mode.DATA;
        if (mode != Mode.INIT) {
            sendNibble(registerSelect, value >> 4);
        }
        sendNibble(registerSelect, value & 0xf);
    }

    private void sendNibble(boolean registerSelect, int value) {
        int status = (registerSelect ? FLAG_REGISTER_SELECT : 0) | (backlightEnabled ? FLAG_BACKLIGHT : 0);
        i2c.write((value << 4) | status | FLAG_ENABLE);
        setDelayMicros(1000);
        i2c.write((value & 0xf0) | status);
        setDelayMicros(1000);
    }

}
