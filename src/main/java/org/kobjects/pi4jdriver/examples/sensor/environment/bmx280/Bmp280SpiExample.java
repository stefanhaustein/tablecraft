/*
 * Forked from
 * https://github.com/Pi4J/pi4j-example-devices/blob/master/src/main/java/com/pi4j/devices/bmp280/BMP280SpiExample.java
 *
 * Original header:
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: EXTENSION
 * FILENAME      :  BMP280SpiExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 *  Copyright (C) 2012 - 2022 Pi4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kobjects.pi4jdriver.examples.sensor.environment.bmx280;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.spi.*;

import org.kobjects.pi4jdriver.sensor.environment.bmx280.Bmx280Driver;

/**
 * Sample application accessing the BMP280 sensor chip via SPI.
 */
public class Bmp280SpiExample {
    
    public static void main(String[] args) throws Exception {

        Context pi4j = Pi4J.newAutoContext();
        Spi spi = pi4j.create(
                Spi.newConfigBuilder(pi4j)
                .bus(SpiBus.BUS_0)
                .chipSelect(SpiChipSelect.CS_0)
                .baud(Spi.DEFAULT_BAUD)    // Max 10MHz
                .mode(SpiMode.MODE_0)
                .provider("linuxfs-spi")
                .build());

        DigitalOutput csPin = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .address(21)
                .shutdown(DigitalState.HIGH)
                .initial(DigitalState.HIGH));

        var bmp280 = Bmx280Driver.create(spi, csPin);

        System.out.println(" Recognized Sensor type: " + bmp280.getSensorType());

        var measurement = bmp280.readMeasurements();
        System.out.println(" Temperatue = " + measurement.getTemperature());
        System.out.println(" Pressure hPa = " + measurement.getPressure());
        System.out.println(" Rel.Humidity % = " + measurement.getHumidity());


        // Shutdown Pi4J
        pi4j.shutdown();
    }

}