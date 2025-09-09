package org.kobjects.pi4jdriver.examples.display.hd44780;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.display.hd44780.Aip31068Connection;
import org.kobjects.pi4jdriver.display.hd44780.Hd44780Driver;


public class Hd44780Example {

    private static final int BUS = 1;
    private static final int DEVICE_ADDRESS =
            0x3e; // Aip31068
    //    0x27;


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        I2C i2c0x27 = pi4J.create(I2C.newConfigBuilder(pi4J)
                .bus(BUS)
                .device(0x27)
                .build());

        I2C i2c0x3e = pi4J.create(I2C.newConfigBuilder(pi4J)
                .bus(BUS)
                .device(0x3e)
                .build());

        Hd44780Driver characterLcd =
//                new Hd44780Driver(new Aip31068Connection(i2c0x3e), 20, 4);
                Hd44780Driver.withPcf8574Connection(i2c0x27, 20, 4);


        characterLcd.setBacklightEnabled(true);
        characterLcd.clearDisplay();
        characterLcd.setBlinkingEnabled(true);
        characterLcd.setCursorEnabled(true);
        characterLcd.write("Hello Wörld (ｼ)\n" + System.currentTimeMillis());
    }
}
