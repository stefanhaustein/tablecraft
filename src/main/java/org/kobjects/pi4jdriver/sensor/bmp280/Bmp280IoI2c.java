/*
 *
 *
 *     *
 *     * -
 *     * #%L
 *     * **********************************************************************
 *     * ORGANIZATION  :  Pi4J
 *     * PROJECT       :  Pi4J :: EXTENSION
 *     * FILENAME      :  BMP280DeviceI2C.java
 *     *
 *     * This file is part of the Pi4J project. More information about
 *     * this project can be found here:  https://pi4j.com/
 *     * **********************************************************************
 *     * %%
 *     *   * Copyright (C) 2012 - 2022 Pi4J
 *      * %%
 *     *
 *     * Licensed under the Apache License, Version 2.0 (the "License");
 *     * you may not use this file except in compliance with the License.
 *     * You may obtain a copy of the License at
 *     *
 *     *      http://www.apache.org/licenses/LICENSE-2.0
 *     *
 *     * Unless required by applicable law or agreed to in writing, software
 *     * distributed under the License is distributed on an "AS IS" BASIS,
 *     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     * See the License for the specific language governing permissions and
 *     * limitations under the License.
 *     * #L%
 *     *
 *
 *
 *
 */

package org.kobjects.pi4jdriver.sensor.bmp280;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.util.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bmp280IoI2c implements Bmp280Io {

    // local/internal I2C reference for communication with hardware chip
    private final  I2C i2c;

    public Bmp280IoI2c(I2C i2c) {
        this.i2c = i2c;
    }

    /**
     * @param register
     * @return 8bit value read from register
     */
    public int readRegister(int register) {
        return i2c.readRegister(register);
    }

    /**
     * @param register register address
     * @param buffer   Buffer to return read data
     * @return count     number bytes read or fail -1
     */
    public int readRegister(int register, byte[] buffer) {
        return i2c.readRegister(register, buffer);
    }

    /**
     * @param register register
     * @param data     byte to write
     * @return bytes written, else -1
     */
    public int writeRegister(int register, int data) {
        return i2c.writeRegister(register, data);
    }
}
