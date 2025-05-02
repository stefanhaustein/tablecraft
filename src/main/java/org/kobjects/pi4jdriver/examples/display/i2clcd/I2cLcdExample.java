package org.kobjects.pi4jdriver.examples.display.i2clcd;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.display.i2clcd.I2cLcdDriver;

public class I2cLcdExample {

    private static final int BUS = 1;
    private static final int DEVICE_ADDRESS = 0x27;

    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        I2C i2c = pi4J.create(I2C.newConfigBuilder(pi4J)
                .bus(BUS)
                .device(DEVICE_ADDRESS)
                .build());

        I2cLcdDriver lcd1602 = new I2cLcdDriver(i2c, 4, 20);

        lcd1602.clearDisplay();
        lcd1602.write("Hello World! " + System.currentTimeMillis());
    }
}
