package org.kobjects.pi4jdriver.display.lcd;

import java.io.Closeable;

public interface LcdIo extends Closeable {

    void setBacklight(boolean backlight);


    /**
     * Write a command to the LCD
     */
    default void sendCommand(int cmd) {
        sendCommand(cmd, 0);
    }

    void sendCommand(int cmd, int mode);
}
