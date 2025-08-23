package org.kobjects.pi4jdriver.examples.display.hd44780;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.display.hd44780.Hd44780Driver;
import org.kobjects.pi4jdriver.display.hd44780.Pcf8574Connection;


public class Hd44780Example {

    private static final int BUS = 1;
    private static final int DEVICE_ADDRESS = 0x27;

    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        I2C i2c = pi4J.create(I2C.newConfigBuilder(pi4J)
                .bus(BUS)
                .device(DEVICE_ADDRESS)
                .build());

        Hd44780Driver characterLcd = new Hd44780Driver(new Pcf8574Connection(i2c), 20, 4);

        characterLcd.clearDisplay();
        characterLcd.setBlinkingEnabled(true);
        characterLcd.setCursorEnabled(true);
        characterLcd.write("Hellö Wörld! äöü§öüäàèàé" + System.currentTimeMillis());
    }
}
