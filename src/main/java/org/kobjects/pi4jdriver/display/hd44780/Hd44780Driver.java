package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.i2c.I2C;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for 16x2 (1602) and similar (20x4/2004...) character LCD.
 *
 * Spec: https://cdn.sparkfun.com/assets/9/5/f/7/b/HD44780.pdf
 *
 * TODO:
 * - Timings according to spec
 * - Virtual width / scroll support
 * - Command support
 * - Unicode mapping
 * - Custom character support
 */
public class Hd44780Driver {

    private static final Map<Integer, Integer> ROM_A00_MAP = generateCharacterMap(
        "βß", 126, "←→", 223, "°αäßɛμσρ", 232, "√", 237, "¢ñö", 242, "Θ∞ΩüΣπ", 254, "÷"
    );

    private static final Map<Integer, Integer> ROM_A02_MAP = generateCharacterMap(
            "АAВBЕEКKМMНHОOРPСCТT",
            8, "◀▶“”\u23eb\u23ec●⏎↑↓←→≤≥▲▼",
            128, "БДЖЗИЙЛПУЦЧШЩЪЫЭα♪ΓπΣστ⍾ΘΩδ∞❤εΠ",
            0xac, "ЮЯ");


    private final int width;
    private final int height;
    private final Output output;
    private final int[] customCharacterToCodePoint = new int[8];
    private final Map<Integer, Integer> codePointToCustomCharacter = new HashMap<>();

    private int cursorX = 0;
    private int cursorY = 0;
    private boolean backlightEnabled = false;
    private boolean cursorEnabled = false;
    private boolean displayEnabled = false;
    private boolean blinkingEnabled = false;

    Hd44780Driver(Output output, int width, int height) {
        this.output = output;
        this.width = width;
        this.height = height;

        // Bring the display into a well-defined state
        delay(35);
        sendCommand(0x03);
        delay(35);
        sendCommand(0x03);
        delay(35);
        sendCommand(0x03);
        delay(35);
        sendCommand(0x02);

        // Initialize display settings

        sendCommand(CMD_FUNCTION_SET | FS_4_BIT | (height == 1 ? FS_1_LINE : FS_2_LINES));
        sendCommand(CMD_ENTRY_MODE | EM_INCREMENT | EM_DISPLAY_SHIFT_OFF);

        setDisplayEnabled(true);
        setBacklightEnabled(true);

        clearDisplay();
        returnHome();
    }

    public Hd44780Driver(I2C pcf8574, int width, int height) {
        this(new Output() {
            @Override
            public void write(int value) {
                pcf8574.write(value);
            }
        }, width, height);
    }

    /**
     * The 7 digital output pins used to control the lcd input pins in the following order:
     * D7, D6, D5, D4, Backlight, Enable, RegisterSelect.
     */
    public Hd44780Driver(DigitalOutput[] digitalOutputs, int width, int height) {
        this (new Output() {
            public void write(int value) {
                for (int i = 0; i < 7; i++) {
                    int mask = (i == 6) ? 1 : (80 >> i);
                    digitalOutputs[i].setState((value & mask) != 0);
                }
            }
        }, width, height);
    }

    public void clearDisplay() {
        sendCommand(CMD_CLEAR_DISPLAY);
        cursorX = 0;
        cursorY = 0;
    }

    /**
     * Returns the Cursor to Home Position (First line, first character)
     */
    public void returnHome() {
        sendCommand(CMD_RETURN_HOME);
        cursorX = 0;
        cursorY = 0;
    }

    public void setBacklightEnabled(boolean backlightEnabled) {
        this.backlightEnabled = backlightEnabled;
        // Any command works here as backlight is a separate pin but we want one without side effect.
        updateDisplayControl();
    }

    public void setCursorEnabled(boolean enabled) {
        this.cursorEnabled = enabled;
        updateDisplayControl();
    }

    public void setDisplayEnabled(boolean enabled) {
        this.displayEnabled = enabled;
        updateDisplayControl();
    }

    public void setBlinkingEnabled(boolean enabled) {
        this.blinkingEnabled = enabled;
        updateDisplayControl();
    }

    public void write(String text) {
        final int length = text.length();
        for (int offset = 0; offset < length; ) {
            final int codepoint = text.codePointAt(offset);
            write(codepoint);
            offset += Character.charCount(codepoint);
        }
    }

    /**
     * Write a character at the current cursor position
     */
    public void write(int codePoint) {
        if (codePoint == '\n' || cursorX >= width) {
            setCursorPosition(0, (cursorY + 1) % height);
            if (codePoint == '\n') {
                return;
            }
        }
        sendData(mapCodePoint(codePoint));
        cursorX++;
    }


    /** Sets the cursor to the given column/row (x/y) position. */
    public void setCursorPosition(int x, int y) {
        if (y >= height || y < 0) {
            throw new IllegalArgumentException("Row " + y + " out of range 0.." + (height - 1));
        }
        if (x >= width || x < 0) {
            throw new IllegalArgumentException("Column " + x + " out of range 0.." + (width - 1));
        }
        sendCommand(CMD_SET_DDRAM_ADDR | x + LCD_ROW_OFFSETS[y]);
        this.cursorX = x;
        this.cursorY = y;
    }

    /**
     * Uploads character data to the given position.
     */
    public void uploadCharacter(int index, long characterData, int codePoint) {
        codePointToCustomCharacter.remove(customCharacterToCodePoint[codePoint]);
        customCharacterToCodePoint[index] = codePoint;
        codePointToCustomCharacter.put(codePoint, index);
        if (index > 7 || index < 1) {
            throw new IllegalArgumentException("Custom character index " + index + " outside valid range (1..7)");
        }
        sendCommand(CMD_SET_CGRAM_ADDR | index << 3);

        for (int i = 0; i < 8; i++) {
            sendData((int) ((characterData >>> ((7-i) * 8)) & 0xffL));
        }
    }

    /**
     * Scroll whole display to the right by one column.
     */
    public void scrollRight(){
        sendCommand(CMD_DISPLAY_SHIFT_RIGHT);
    }

    /**
     * Scroll whole display to the left by one column.
     */
    public void scrollLeft(){
        sendCommand(CMD_DISPLAY_SHIFT_LEFT);
    }


    // Private helpers

    private static Map<Integer, Integer> generateCharacterMap(String lookalikes, Object... data) {
        int index = 0;
        Map<Integer, Integer> map = new HashMap<>();
        while (index < data.length) {
            int target = (Integer) data[index++];
            String values = (String) data[index++];
            for (int i = 0; i < values.length(); i++) {
                map.put((int) values.charAt(i), target + i);
            }
        }
        for (int i = 0; i < lookalikes.length(); i+=2) {
            int target = lookalikes.charAt(i+1);
            map.put((int) lookalikes.charAt(i), map.getOrDefault(target, target));
        }
        return map;
    }


    private int mapCodePoint(int codePoint) {
        Integer custom = codePointToCustomCharacter.get(codePoint);
        if (custom != null) {
            return custom;
        }
        Integer rom = ROM_A00_MAP.get(codePoint);
        if (rom != null) {
            return rom;
        }
        return codePoint;
    }

    private void updateDisplayControl() {
        sendCommand(CMD_DISPLAY_CONTROL
                | (displayEnabled ? DC_DISPLAY_ON : DC_DISPLAY_OFF)
                | (cursorEnabled ? DC_CURSOR_ON : DC_CURSOR_OFF)
                | (blinkingEnabled ? DC_BLINK_ON : DC_BLINK_OFF)
        );
    }

    private void sendCommand(int id) {
       sendValue(id, false);
    }

    private void sendData(int value) {
        sendValue(value, true);
    }

    private void sendValue(int value, boolean registerSelect) {
        int status = (registerSelect ? FLAG_REGISTER_SELECT : 0) | (backlightEnabled ? FLAG_BACKLIGHT : 0);

        output.write((value & 0xf0) | status | FLAG_ENABLE);
        delay(1);
        output.write((value & 0xf0) | status);
        delay(1);
        output.write((value & 0x0f) << 4 | status | FLAG_ENABLE);
        delay(1);
        output.write((value & 0x0f) << 4 | status);
        delay(1);
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    interface Output {
        void write(int value);
    }


    private final int FLAG_REGISTER_SELECT = 0b0000_0001;  // Set when writing text/data (opposed to command)
    private final int FLAG_READ_WRITE =      0b0000_0010;
    private final int FLAG_ENABLE =          0b0000_0100;
    private final int FLAG_BACKLIGHT =       0b0000_1000;


    /** Display commands and their flags. */
    private static final int CMD_CLEAR_DISPLAY   = 0x01;
    private static final int CMD_RETURN_HOME     = 0x02;

    private static final int CMD_ENTRY_MODE       = 0x04;
    private static final int EM_INCREMENT         = 0x02;
    private static final int EM_DECREMENT         = 0x00;
    private static final int EM_DISPLAY_SHIFT_ON  = 0x01;
    private static final int EM_DISPLAY_SHIFT_OFF = 0x00;

    private static final int CMD_DISPLAY_CONTROL = 0x08;
    private static final int DC_DISPLAY_ON       = 0x04;
    private static final int DC_DISPLAY_OFF      = 0x00;
    private static final int DC_CURSOR_ON        = 0x02;
    private static final int DC_CURSOR_OFF       = 0x00;
    private static final int DC_BLINK_ON         = 0x01;
    private static final int DC_BLINK_OFF        = 0x00;

    private static final int CMD_CURSOR_SHIFT_LEFT   = 0x0001_0000;
    private static final int CMD_CURSOR_SHIFT_RIGHT  = 0x0001_0100;
    private static final int CMD_DISPLAY_SHIFT_LEFT  = 0x0001_1000;
    private static final int CMD_DISPLAY_SHIFT_RIGHT = 0x0001_1100;

    private static final int CMD_FUNCTION_SET    = 0x20;
    private static final int FS_8_BIT            = 0x10;
    private static final int FS_4_BIT            = 0x00;
    private static final int FS_2_LINES          = 0x08;
    private static final int FS_1_LINE           = 0x00;
    private static final int FS_CHARSET_5X10     = 0x04;
    private static final int FS_CHARSET_5X8      = 0x00;

    private static final int CMD_SET_CGRAM_ADDR  = 0x40;
    private static final int CMD_SET_DDRAM_ADDR  = 0x80;

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
}
