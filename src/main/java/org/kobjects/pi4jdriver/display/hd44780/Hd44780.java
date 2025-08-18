package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.i2c.I2C;

/**
 * Used for 16x2 (1602) and similar (20x4/2004...) text LCD displays.
 *
 * Spec: https://cdn.sparkfun.com/assets/9/5/f/7/b/HD44780.pdf
 */
public class Hd44780 {

    private final int BACKLIGHT =       0b0000_1000;
    private final int REGISTER_SELECT = 0b0000_0100;  // Set when writing text/data (opposed to command)
    private final int READ_WRITE =      0b0000_0010;
    private final int ENABLE =          0b0000_0001;

    private final Output output;
    private boolean backlight = false;

    Hd44780(Output output) {
        this.output = output;
    }

    public Hd44780(I2C pcf8574) {
        this(new Output() {
            @Override
            public void write(int value) {
                pcf8574.write(value);
            }
        });
    }

    /**
     * The 7 digital output pins used to control the lcd input pins in the following order:
     * D7, D6, D5, D4, Backlight, RegisterSelect, Enable.
     */
    public Hd44780(DigitalOutput... digitalOutputs) {
        this (new Output() {
            public void write(int value) {
                for (int i = 0; i < 7; i++) {
                    int mask = (i == 6) ? 1 : (80 >> i);
                    digitalOutputs[i].setState((value & mask) != 0);
                }
            }
        });
    }

    private void sendCommand(int id) {
       sendValue(id, false);
    }

    private void sendData(int value) {
        sendValue(value, true);
    }

    private void sendValue(int value, boolean registerSelect) {
        int status = (registerSelect ? REGISTER_SELECT : 0) | (backlight ? BACKLIGHT : 0);

        output.write((value & 0xf0) | status | ENABLE);
        sleep(1);
        output.write((value & 0xf0) | status);
        sleep(1);
        output.write((value & 0x0f) << 4 | status | ENABLE);
        sleep(1);
        output.write((value & 0x0f) << 4 | status);
        sleep(1);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    interface Output {
        void write(int value);
    }
}
