package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.i2c.I2C;

public class Pcf8574Connection extends AbstractConnection {
    // PCF8574 supports 8 addresses in increments of 2.
    public static final int I2C_ADDRESS_BASE = 0x40;
    public static final int I2C_ADDRESS_COUNT = 8;
    public static final int I2C_ADDRESS_STEP = 2;

    private static final int FLAG_REGISTER_SELECT = 0b0000_0001;  // Set when writing text/data (opposed to command)
    private static final int FLAG_READ_WRITE =      0b0000_0010;
    private static final int FLAG_ENABLE =          0b0000_0100;
    private static final int FLAG_BACKLIGHT =       0b0000_1000;

    private final I2C i2c;

    private boolean backlightEnabled;

    public Pcf8574Connection(I2C i2C) {
        this.i2c = i2C;
        // Bring the display into a well-defined 4 bit state
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x02);
    }


    @Override
    public void setBacklight(boolean on) {
        this.backlightEnabled = on;
    }


    @Override
    public void sendValue(boolean registerSelect, int value) {
        int status = (registerSelect ? FLAG_REGISTER_SELECT : 0) | (backlightEnabled ? FLAG_BACKLIGHT : 0);

        i2c.write((value & 0xf0) | status | FLAG_ENABLE);
        delay(1);
        i2c.write((value & 0xf0) | status);
        delay(1);
        i2c.write((value & 0x0f) << 4 | status | FLAG_ENABLE);
        delay(1);
        i2c.write((value & 0x0f) << 4 | status);
        delay(1);
    }
}
