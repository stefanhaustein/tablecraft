/*
 * Forked from
 * https://github.com/Pi4J/pi4j-example-components/blob/main/src/main/java/com/pi4j/catalog/components/LcdDisplay.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kobjects.pi4jdriver.display.lcd;

import com.pi4j.io.i2c.I2C;

/** Pcf8574-based lcd i2c io command implementation. */
public class Pcf8574LcdIo implements LcdIo {

    // flags for backlight control
    private static final byte LCD_BACKLIGHT     = (byte) 0x08;
    private static final byte LCD_NO_BACKLIGHT  = (byte) 0x00;

    private static final byte En                = (byte) 0b000_00100; // Enable bit
    private static final byte Rw                = (byte) 0b000_00010; // Read/Write bit
    private static final byte Rs                = (byte) 0b000_00001; // Register select bit

    private final I2C i2c;
    private boolean backlight;

    private long delayTarget;

    public Pcf8574LcdIo(I2C i2c) {
        this.i2c = i2c;

        delay(35);
        sendCommand((byte) 0x03);
        delay(35);
        sendCommand((byte) 0x03);
        delay(35);
        sendCommand((byte) 0x03);
        delay(35);
        sendCommand((byte) 0x02);
        delay(35);
    }

    /**
     * send a single command to device
     */

    private void rawCommand(int cmd) {
        observeDelay();
        i2c.write((byte) cmd);
        delay(2);
    }


    @Override
    public void setBacklight(boolean backlight) {
        this.backlight = backlight;
        rawCommand(backlight ? LCD_BACKLIGHT : LCD_NO_BACKLIGHT);
    }

    /**
     * Write a command in 2 parts to the LCD
     */
    @Override
    public void sendCommand(int cmd, int mode) {
        observeDelay();

        //bitwise AND with 11110000 to remove last 4 bits
        writeNibble((byte) (mode | (cmd & 0xF0)));
        //bitshift and bitwise AND to remove first 4 bits
        writeNibble((byte) (mode | ((cmd << 4) & 0xF0)));

        delay(1);
    }

    /**
     * Write the four bits of a byte to the LCD
     *
     * @param data the byte that is sent
     */
    private void writeNibble(byte data) {
        byte backlightStatus = backlight ? LCD_BACKLIGHT : LCD_NO_BACKLIGHT;

        i2c.write((byte) (data | En | backlightStatus));
        i2c.write((byte) ((data & ~En) | backlightStatus));
    }


    // Private methods

    private void delay(int ms) {
        delayTarget = Math.max(delayTarget, System.currentTimeMillis() + ms);
    }

    private void observeDelay() {
        while (true) {
            long remaining = delayTarget - System.currentTimeMillis();
            if (remaining <= 0) {
                return;
            }
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Shuts the display off
     */
    public void close() {
        rawCommand(0);
        i2c.close();
    }

}