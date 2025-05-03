package org.kobjects.pi4jdriver.examples.display.lcd;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.display.lcd.LcdDriver;
import org.kobjects.pi4jdriver.display.lcd.Pcf8574LcdIo;

public class I2cLcdExample {

    private static final int BUS = 1;
    private static final int DEVICE_ADDRESS = 0x27;

    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        I2C i2c = pi4J.create(I2C.newConfigBuilder(pi4J)
                .bus(BUS)
                .device(DEVICE_ADDRESS)
                .build());

        LcdDriver lcd1602 = LcdDriver.create(i2c, 4, 20);

        lcd1602.clearDisplay();
        lcd1602.write("Hello World! " + System.currentTimeMillis());
    }
}
