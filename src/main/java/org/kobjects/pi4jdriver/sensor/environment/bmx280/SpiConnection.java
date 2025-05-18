/*
 *
 *
 *     *
 *     * -
 *     * #%L
 *     * **********************************************************************
 *     * ORGANIZATION  :  Pi4J
 *     * PROJECT       :  Pi4J :: EXTENSION
 *     * FILENAME      :  BMP280DeviceSPI.java
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
package org.kobjects.pi4jdriver.sensor.environment.bmx280;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.*;


/*
SPI operates mode0 or mode1
Register address use the MSB to indicate read (1) or write (0)
Access register 0x7f
    Read access transfer 0xf7
    Write access transfer 0x77

 Write 2 bytes to register 0xF7
 Send  0x77  byte1   0x77 byte2

 Read two bytes from register 0xf7
 Send 0xf7  rcv byte1  rcv byte2


 */

class SpiConnection extends AbstractConnection {
    private final Spi spi;
    private final DigitalOutput csGpio;

    public SpiConnection(Spi spi, DigitalOutput csGpio) {
        this.spi = spi;
        this.csGpio = csGpio;
    }

    /**
     * @param register
     * @return 8bit value read from register
     */
    public int readU8Register(int register) {
        csGpio.low();
        spi.write((byte) (0b10000000 | register));
        byte rval = this.spi.readByte();
        csGpio.high();
        return rval;
    }

    /**
     * @param register register address
     * @param buffer   Buffer to return read data
     * @return count     number bytes read or fail -1
     */
    public int readRegister(int register, byte[] buffer) {
        this.csGpio.low();
        this.spi.write((byte) (0b10000000 | register));
        int bytesRead = spi.read(buffer);
        this.csGpio.high();
        return bytesRead;
    }


    /**
     * @param register register
     * @param data     byte to write
     * @return bytes written, else -1
     */
    public int writeU8Register(int register, int data) {
        // send read request to BMP chip via SPI channel
        this.csGpio.low();
        int byteswritten = spi.write((byte) (0b01111111 & register), (byte) data);
        this.csGpio.high();
        return byteswritten;
    }
}

