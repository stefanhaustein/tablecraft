/*
 *  Forked from
 *  https://github.com/Pi4J/pi4j-example-devices/blob/master/src/main/java/com/pi4j/devices/bmp280/BMP280I2cExample.java
 *
 *  Original header:
 *  **********************************************************************
 *  ORGANIZATION  :  Pi4J
 *  PROJECT       :  Pi4J ::  Providers
 *  FILENAME      :  BMP280I2cExample.java
 *
 *  This file is part of the Pi4J project. More information about
 *  this project can be found here:  https://pi4j.com/
 *  **********************************************************************
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU General Lesser Public
 *  License along with this program.  If not, see
 *  <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */
package org.kobjects.pi4jdriver.examples.sensor.environment.bmx280;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import org.kobjects.pi4jdriver.sensor.environment.bmx280.Bmx280Driver;


/**
 * Sample application using BMP280 sensor chip via i2c.
 */
public class Bmp280I2cExample {

    private static final int I2C_BUS = 1;
    private static final int I2C_ADDRESS = 0x77;

    public static void main(String[] args) throws Exception {
        
        Context pi4j = Pi4J.newAutoContext();
        
        I2C i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                .bus(I2C_BUS)
                .device(I2C_ADDRESS)
                .provider("linuxfs-i2c")
                .build());

        Bmx280Driver bmp280 = new Bmx280Driver(i2c);


         bmp280.reset();

         for (int i = 0; i < 10; i++) {
            Bmx280Driver.Measurement measurement = bmp280.readMeasurements();

            System.out.println(" Sensor Type: " + bmp280.getSensorType());

            System.out.println(" Temperature C = " + measurement.getTemperature());
            System.out.println(" Pressure Pa = " + measurement.getPressure());
            System.out.println(" Rel.Humidity % = " + measurement.getHumidity());
         }
         
        // Shutdown Pi4J
        pi4j.shutdown();
    }

}