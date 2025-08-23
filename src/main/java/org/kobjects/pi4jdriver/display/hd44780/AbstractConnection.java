package org.kobjects.pi4jdriver.display.hd44780;

/** Connection Abstraction for the Hd44780 driver. */
public abstract class AbstractConnection {
    protected abstract void setBacklight(boolean on);
    protected abstract void sendValue(boolean rs, int value);

    // Placing this here allows coordination of chip and connection based delays without needing a driver
    // reference here or complex interactions.
    void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
