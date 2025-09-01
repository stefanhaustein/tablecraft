package org.kobjects.pi4jdriver.examples.display.pcd8544;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import org.kobjects.pi4jdriver.display.pcd8544.Constants;
import org.kobjects.pi4jdriver.display.pcd8544.Pcd8544Driver;

public class Pcd8544Example {

    public static void main(String[] args) throws Exception {

        Context pi4J = Pi4J.newAutoContext();

        Spi spi = pi4J.create(Spi.newConfigBuilder(pi4J)
                .bus(0)
                .chipSelect(SpiChipSelect.CS_0)
                        .baud(4000000)
                .build());

        DigitalOutput rst = pi4J.create(DigitalOutput.newConfigBuilder(pi4J).initial(DigitalState.HIGH).address(24).build());
        DigitalOutput dc = pi4J.create(DigitalOutput.newConfigBuilder(pi4J).address(23).build());

        Pcd8544Driver driver = new Pcd8544Driver(spi, rst, dc);

        byte[] px = new byte[40];
        for (int i = 0; i < px.length; i++) {
            px[i] = (byte) i;
        }
        for (int y = 0; y < 8; y++) {
            driver.setPixels(0, y, px);
        }

        while(true) {
            Thread.sleep(1000);
            driver.command(Constants.DISPLAY_CONTROL | Constants.DC_ALLON);
            Thread.sleep(1000);
            driver.command(Constants.DISPLAY_CONTROL | Constants.DC_BLANK);
        }

    }
}
