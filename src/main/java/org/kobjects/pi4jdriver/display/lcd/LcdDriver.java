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

import java.io.Closeable;
import java.io.IOException;

/**
 * Implementation of a LCDDisplay using GPIO with Pi4J
 * <p>
 * Works with the PCF8574T backpack, only.
 */
public class LcdDriver implements Closeable {

    /** Flags for display commands */
    private static final int LCD_CLEAR_DISPLAY   = 0x01;
    private static final int LCD_RETURN_HOME     = 0x02;
    private static final int LCD_SCROLL_RIGHT    = 0x1E;
    private static final int LCD_SCROLL_LEFT     = 0x18;
    private static final int LCD_ENTRY_MODE_SET  = 0x04;
    private static final int LCD_DISPLAY_CONTROL = 0x08;
    private static final int LCD_CURSOR_SHIFT    = 0x10;
    private static final int LCD_FUNCTION_SET    = 0x20;
    private static final int LCD_SET_CGRAM_ADDR  = 0x40;
    private static final int LCD_SET_DDRAM_ADDR  = 0x80;

    // flags for display entry mode
    private static final int LCD_ENTRY_RIGHT           = 0x00;
    private static final int LCD_ENTRY_LEFT            = 0x02;
    private static final int LCD_ENTRY_SHIFT_INCREMENT = 0x01;
    private static final int LCD_ENTRY_SHIFT_DECREMENT = 0x00;

    // flags for display on/off control
    private static final int LCD_DISPLAY_ON  = 0x04;
    private static final int LCD_DISPLAY_OFF = 0x00;
    private static final int LCD_CURSOR_ON   = 0x02;
    private static final int LCD_CURSOR_OFF  = 0x00;
    private static final int LCD_BLINK_ON    = 0x01;
    private static final int LCD_BLINK_OFF   = 0x00;

    // flags for display/cursor shift
    private static final int LCD_DISPLAY_MOVE = 0x08;
    private static final int LCD_CURSOR_MOVE  = 0x00;

    // flags for function set
    private static final int LCD_8BIT_MODE = 0x10;
    private static final int LCD_4BIT_MODE = 0x00;
    private static final int LCD_2LINE     = 0x08;
    private static final int LCD_1LINE     = 0x00;
    private static final int LCD_5x10DOTS  = 0x04;
    private static final int LCD_5x8DOTS   = 0x00;

    /** Display row offsets for up to 4 rows. */
    private static final byte[] LCD_ROW_OFFSETS = {0x00, 0x40, 0x14, 0x54};

    private static final byte Rs                = 0b000_00001; // Register select bit


    private final LcdIo io;

    /** Number of rows available */
    private final int rows;

    /** Number of columns available */
    private final int columns;

    /** The current row */
    private int row;

    /** The current column */
    private int column;


    /** Creates a new Lcd1602 driver for a display of the given dimensions; up to 4x20 */
    public LcdDriver(LcdIo io, int rows, int columns) {
        this.io = io;
        this.rows = rows;
        this.columns = columns;

        // Initialize display settings
        io.sendCommand((LCD_FUNCTION_SET | LCD_2LINE | LCD_5x8DOTS | LCD_4BIT_MODE));
        io.sendCommand((LCD_DISPLAY_CONTROL | LCD_DISPLAY_ON | LCD_CURSOR_OFF | LCD_BLINK_OFF));
        io.sendCommand((LCD_ENTRY_MODE_SET | LCD_ENTRY_LEFT | LCD_ENTRY_SHIFT_DECREMENT));

        clearDisplay();

        // Enable backlight
        io.setBacklight(true);
    }

    /**
     * Turns the backlight on or off
     */
    public void setBacklight(boolean backlightEnabled) {
        io.setBacklight(backlightEnabled);

    }

    /**
     * Clear the LCD and set cursor to home
     */
    public void clearDisplay() {
        home();
        io.sendCommand(LCD_CLEAR_DISPLAY);
    }

    /**
     * Returns the Cursor to Home Position (First line, first character)
     */
    public void home() {
        io.sendCommand(LCD_RETURN_HOME);
        column = 0;
        row = 0;
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
        io.sendCommand(character, Rs);
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
        io.sendCommand(LCD_SET_DDRAM_ADDR | column + LCD_ROW_OFFSETS[row]);
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
        io.sendCommand((LCD_SET_CGRAM_ADDR | location << 3));

        for (int i = 0; i < 8; i++) {
            io.sendCommand(character[i], 1);
        }
    }

    /**
     * Scroll whole display to the right by one column.
     */
    public void scrollRight(){
        io.sendCommand(LCD_SCROLL_RIGHT);
    }

    /**
     * Scroll whole display to the left by one column.
     */
    public void scrollLeft(){
        io.sendCommand(LCD_SCROLL_LEFT);
    }


    @Override
    public void close() throws IOException {
        io.close();
    }
}