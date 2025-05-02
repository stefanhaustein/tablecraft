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

package org.kobjects.pi4jdriver.display.i2clcd;


import com.pi4j.io.i2c.I2C;

/**
 * Implementation of a LCDDisplay using GPIO with Pi4J
 * <p>
 * Works with the PCF8574T backpack, only.
 */
public class I2cLcdDriver {

    /** Flags for display commands */
    private static final byte LCD_CLEAR_DISPLAY   = (byte) 0x01;
    private static final byte LCD_RETURN_HOME     = (byte) 0x02;
    private static final byte LCD_SCROLL_RIGHT    = (byte) 0x1E;
    private static final byte LCD_SCROLL_LEFT     = (byte) 0x18;
    private static final byte LCD_ENTRY_MODE_SET  = (byte) 0x04;
    private static final byte LCD_DISPLAY_CONTROL = (byte) 0x08;
    private static final byte LCD_CURSOR_SHIFT    = (byte) 0x10;
    private static final byte LCD_FUNCTION_SET    = (byte) 0x20;
    private static final byte LCD_SET_CGRAM_ADDR  = (byte) 0x40;
    private static final byte LCD_SET_DDRAM_ADDR  = (byte) 0x80;

    // flags for display entry mode
    private static final byte LCD_ENTRY_RIGHT           = (byte) 0x00;
    private static final byte LCD_ENTRY_LEFT            = (byte) 0x02;
    private static final byte LCD_ENTRY_SHIFT_INCREMENT = (byte) 0x01;
    private static final byte LCD_ENTRY_SHIFT_DECREMENT = (byte) 0x00;

    // flags for display on/off control
    private static final byte LCD_DISPLAY_ON  = (byte) 0x04;
    private static final byte LCD_DISPLAY_OFF = (byte) 0x00;
    private static final byte LCD_CURSOR_ON   = (byte) 0x02;
    private static final byte LCD_CURSOR_OFF  = (byte) 0x00;
    private static final byte LCD_BLINK_ON    = (byte) 0x01;
    private static final byte LCD_BLINK_OFF   = (byte) 0x00;

    // flags for display/cursor shift
    private static final byte LCD_DISPLAY_MOVE = (byte) 0x08;
    private static final byte LCD_CURSOR_MOVE  = (byte) 0x00;

    // flags for function set
    private static final byte LCD_8BIT_MODE = (byte) 0x10;
    private static final byte LCD_4BIT_MODE = (byte) 0x00;
    private static final byte LCD_2LINE     = (byte) 0x08;
    private static final byte LCD_1LINE     = (byte) 0x00;
    private static final byte LCD_5x10DOTS  = (byte) 0x04;
    private static final byte LCD_5x8DOTS   = (byte) 0x00;

    // flags for backlight control
    private static final byte LCD_BACKLIGHT     = (byte) 0x08;
    private static final byte LCD_NO_BACKLIGHT  = (byte) 0x00;

    private static final byte En                = (byte) 0b000_00100; // Enable bit
    private static final byte Rw                = (byte) 0b000_00010; // Read/Write bit
    private static final byte Rs                = (byte) 0b000_00001; // Register select bit

    /** Display row offsets for up to 4 rows. */
    private static final byte[] LCD_ROW_OFFSETS = {0x00, 0x40, 0x14, 0x54};

    private final I2C i2c;

    /** Number of rows available */
    private final int rows;

    /** Number of columns available */
    private final int columns;

    /** True if the backlight is currently on */
    private boolean backlight;

    /** The current row */
    private int row;

    /** The current column */
    private int column;

    private long delayTarget;

    /** Creates a new Lcd1602 driver for a display of the given dimensions; up to 4x20 */
    public I2cLcdDriver(I2C i2c, int rows, int columns) {
        this.i2c = i2c;
        this.rows = rows;
        this.columns = columns;

        delay(35);
        sendTwoPartsCommand((byte) 0x03);
        delay(35);
        sendTwoPartsCommand((byte) 0x03);
        delay(35);
        sendTwoPartsCommand((byte) 0x03);
        delay(35);
        sendTwoPartsCommand((byte) 0x02);
        delay(35);
        // Initialize display settings
        sendTwoPartsCommand((byte) (LCD_FUNCTION_SET | LCD_2LINE | LCD_5x8DOTS | LCD_4BIT_MODE));
        sendTwoPartsCommand((byte) (LCD_DISPLAY_CONTROL | LCD_DISPLAY_ON | LCD_CURSOR_OFF | LCD_BLINK_OFF));
        sendTwoPartsCommand((byte) (LCD_ENTRY_MODE_SET | LCD_ENTRY_LEFT | LCD_ENTRY_SHIFT_DECREMENT));

        clearDisplay();

        // Enable backlight
        setBacklight(true);
    }

    /**
     * Turns the backlight on or off
     */
    public void setBacklight(boolean backlightEnabled) {
        backlight = backlightEnabled;
        sendCommand(backlight ? LCD_BACKLIGHT : LCD_NO_BACKLIGHT);
    }

    /**
     * Clear the LCD and set cursor to home
     */
    public void clearDisplay() {
        home();
        sendTwoPartsCommand(LCD_CLEAR_DISPLAY);
    }

    /**
     * Returns the Cursor to Home Position (First line, first character)
     */
    public void home() {
        sendTwoPartsCommand(LCD_RETURN_HOME);
        column = 0;
        row = 0;
    }

    /**
     * Shuts the display off
     */
    public void off() {
        sendCommand(LCD_DISPLAY_OFF);
    }

    public void write(String text) {
        for (int i = 0; i < text.length(); i++) {
            writeCharacter(text.charAt(i));
        }
    }

    /**
     * Write a character at the current cursor position
     */
    public void writeCharacter(char character) {
        if (character == '\n' || column >= columns) {
            setCursorPosition((row + 1) % rows, 0);
            if (character == '\n') {
                return;
            }
        }
        sendTwoPartsCommand((byte) character, Rs);
        column++;
    }


    /** Sets the cursor to the given position. */
    public void setCursorPosition(int row, int column) {
        if (row >= rows || row < 0) {
            throw new IllegalArgumentException("Row " + row + " out of range 0.." + (rows - 1));
        }
        if (column >= columns || column < 0) {
            throw new IllegalArgumentException("Column " + column + " out of range 0.." + (columns - 1));
        }
        sendTwoPartsCommand(LCD_SET_DDRAM_ADDR | column + LCD_ROW_OFFSETS[row]);
        this.column = column;
        this.row = row;
    }

    /**
     * Create a custom character by providing the single digit states of each pixel. Simply pass an Array of bytes
     * which will be translated to a character.
     *
     * @param location  Set the memory location of the character. 1 - 7 is possible.
     * @param character Byte array representing the pixels of a character
     */
    public void createCharacter(int location, byte[] character) {
        if (character.length != 8) {
            throw new IllegalArgumentException("Array has invalid length. Character is only 5x8 Digits. Only a array with length" +
                    " 8 is allowed");
        }

        if (location > 7 || location < 1) {
            throw new IllegalArgumentException("Invalid memory location. Range 1-7 allowed. Value: " + location);
        }
        sendTwoPartsCommand((byte) (LCD_SET_CGRAM_ADDR | location << 3));

        for (int i = 0; i < 8; i++) {
            sendTwoPartsCommand(character[i], (byte) 1);
        }
    }

    /**
     * Scroll whole display to the right by one column.
     */
    public void scrollRight(){
        sendTwoPartsCommand(LCD_SCROLL_RIGHT);
    }

    /**
     * Scroll whole display to the left by one column.
     */
    public void scrollLeft(){
        sendTwoPartsCommand(LCD_SCROLL_LEFT);
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
     * send a single command to device
     */
    private void sendCommand(byte cmd) {
        observeDelay();
        i2c.write(cmd);
        delay(2);
    }


    /**
     * Write a command to the LCD
     */
    private void sendTwoPartsCommand(int cmd) {
        sendTwoPartsCommand(cmd, 0);
    }

    /**
     * Write a command in 2 parts to the LCD
     */
    private void sendTwoPartsCommand(int cmd, int mode) {
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

}