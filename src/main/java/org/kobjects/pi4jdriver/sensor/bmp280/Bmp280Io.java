/*
 *  **********************************************************************
 *  ORGANIZATION  :  Pi4J
 *  PROJECT       :  Pi4J ::  Providers
 *  FILENAME      :  TemperatureSensorIntf.java
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
package org.kobjects.pi4jdriver.sensor.bmp280;

/**
 * <p>BMP280 Sensor interface.</p>
 *
 * @author Tom Aarts
 * @version $Id: $Id
 * <p>
 * This class permits the BMP280 device to create additional methods specific to the
 * BMP280 .
 */
public interface Bmp280Io {

    /**
     * @param register
     * @return 8bit value read from register
     */
    int readRegister(int register);

    /**
     *
     * @param register   Multi byte register address
     * @param buffer     Buffer to return read data
     * @return count     number bytes read or fail -1
     */
    //   int readRegister(byte[] register, byte[] buffer);

    /**
     * @param register register address
     * @param buffer   Buffer to return read data
     * @return count     number bytes read or fail -1
     */
    int readRegister(int register, byte[] buffer);

    /**
     * @param register byte register
     * @param data     byte data to write
     * @return bytes written, else -1
     */
    int writeRegister(int register, int data);

}


