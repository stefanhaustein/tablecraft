package org.kobjects.pi4jdriver.examples.sensor.scd4x;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.sensor.scd4x.Scd4xDriver;

public class Scd4xExample {

    public final static int I2C_BUS = 1;

    public static void main(String[] args) throws InterruptedException  {

        Context pi4j = Pi4J.newAutoContext();

        I2C i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                .bus(I2C_BUS)
                .device(Scd4xDriver.I2C_ADDRESS)
                .provider("linuxfs-i2c")
                .build());

        Scd4xDriver driver = new Scd4xDriver(i2c);

        driver.stopPeriodicMeasurement();

        driver.reInit();

        driver.startPeriodicMeasurement();

        while (true) {
            Thread.sleep(5000);

            boolean ready = driver.getDataReadyStatus();

            System.out.println("Ready state: " + ready);

            if (ready) {
                break;
            }
        }
        System.out.println(driver.readMeasurement());


    }

}
