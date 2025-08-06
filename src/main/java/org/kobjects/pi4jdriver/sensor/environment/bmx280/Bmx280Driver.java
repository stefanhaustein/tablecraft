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

import com.pi4j.io.i2c.I2CRegisterDataReaderWriter;
import com.pi4j.io.spi.Spi;

// Re-implementation based on I2CRegisterDataReaderWriter
public class Bmx280Driver {

    private final byte[] ioBuf = new byte[2];

    private final I2CRegisterDataReaderWriter registerAccess;
    private final SensorType sensorType;
    private MeasurementMode measurementMode = MeasurementMode.SLEEPING;

    private long sleepUntil = 0;
    // Calibration values for humidity, pressure and temperature
    private final int dig_h1, dig_h2, dig_h3, dig_h4, dig_h5, dig_h6;
    private final int dig_p1, dig_p2, dig_p3, dig_p4, dig_p5, dig_p6, dig_p7, dig_p8, dig_p9;
    private final int dig_t1, dig_t2, dig_t3;


    private final byte[] measurementBuf;

    public Bmx280Driver(Spi spi) {
        this (new SpiRegisterAccess(spi));
    }

    public Bmx280Driver(I2CRegisterDataReaderWriter registerAccess) {
        this.registerAccess = registerAccess;

        int id = readU8Register(Bmp280Constants.CHIP_ID);
        //SensorType sensorType;
        if (id == Bmp280Constants.ID_VALUE_MSK_BMP) {
            sensorType = SensorType.BMP280;
            measurementBuf = new byte[6];
            dig_h1 = dig_h2 = dig_h3 = dig_h4 = dig_h5 = dig_h6 = 0;

        } else if (id == Bmp280Constants.ID_VALUE_MSK_BME) {
            sensorType = SensorType.BME280;
            measurementBuf = new byte[8];

            dig_h1 = readU8Register(Bme280Constants.REG_DIG_H1);
            dig_h2 = readS16Register(Bme280Constants.REG_DIG_H2);
            dig_h3 = readU8Register(Bme280Constants.REG_DIG_H3);

            int e4 = readU8Register(0xe4);
            int e5 = readU8Register(0xe5);

            int h4_hsb = e4 * 16;
            int h4_lsb = e5 & 0x0f;
            dig_h4 = h4_hsb | h4_lsb;

            int e6 = readU8Register(0xe6);

            int h5_lsb = e5 >> 4;
            int h5_hsb = e6 * 16;
            dig_h5 = h5_hsb | h5_lsb;

            dig_h6 = readS8Register(Bme280Constants.REG_DIG_H6);
        } else {
            throw new IllegalStateException("Incorrect chip ID read");
        }

        // Read calibaration values.

        dig_t1 = readU16Register(Bmp280Constants.REG_DIG_T1);
        dig_t2 = readS16Register(Bmp280Constants.REG_DIG_T2);
        dig_t3 = readS16Register(Bmp280Constants.REG_DIG_T3);

        dig_p1 = readU16Register(Bmp280Constants.REG_DIG_P1);
        dig_p2 = readS16Register(Bmp280Constants.REG_DIG_P2);
        dig_p3 = readS16Register(Bmp280Constants.REG_DIG_P3);
        dig_p4 = readS16Register(Bmp280Constants.REG_DIG_P4);
        dig_p5 = readS16Register(Bmp280Constants.REG_DIG_P5);
        dig_p6 = readS16Register(Bmp280Constants.REG_DIG_P6);
        dig_p7 = readS16Register(Bmp280Constants.REG_DIG_P7);
        dig_p8 = readS16Register(Bmp280Constants.REG_DIG_P8);
        dig_p9 = readS16Register(Bmp280Constants.REG_DIG_P9);
    }

    /**
     *
     * Configure BMP280 for 1x oversamplimg and single measurement.
     */

    public void requestSingleMeasurement() {
        materializeSleep();

        // set forced mode to leave sleep mode state and initiate measurements.
        // At measurement completion chip returns to sleep mode

        if (sensorType == SensorType.BME280) {
            int ctlHum = readU8Register(Bme280Constants.CTRL_HUM);
            ctlHum = (ctlHum & ~Bme280Constants.CTRL_HUM_MSK) | Bme280Constants.CTRL_HUM_SAMP_1;
            writeU8Register(Bme280Constants.CTRL_HUM, ctlHum);
        }

        int ctlReg = 0; // bus.readU8Register(Bmp280Constants.CTRL_MEAS);
        ctlReg |= Bmp280Constants.POWERMODE_FORCED;
        //ctlReg &= ~Bmp280Constants.TEMP_OVER_SAMPLE_MSK;   // mask off all temperature bits
        ctlReg |= Bmp280Constants.OVERSAMPLING_1X << Bmp280Constants.CTRL_TEMP_POS;      // Temperature oversample 1
        //ctlReg &= ~Bmp280Constants.PRES_OVER_SAMPLE_MSK;   // mask off all pressure bits
        ctlReg |= Bmp280Constants.OVERSAMPLING_1X << Bmp280Constants.CTRL_PRESS_POS;   //  Pressure oversample 1

        writeU8Register(Bmp280Constants.CTRL_MEAS,  ctlReg);

        measurementMode = MeasurementMode.SINGLE;

        sleepUntil = System.currentTimeMillis() + 1000;
    }


    /**
     * Read and store all factory set conversion data.
     * Read measure registers 0xf7 - 0xFC in single read to ensure all the data pertains to
     * a single  measurement.
     * <p>
     * Use conversion data and measure data to calculate temperature in C and pressure in Pa.
     * <p>
     * Store the measured data.
     */
    public Bmx280Measurement readMeasurements() {
        if (measurementMode == MeasurementMode.SLEEPING) {
            requestSingleMeasurement();
        }
        materializeSleep();
        readRegister(Bmp280Constants.PRESS_MSB, measurementBuf);

        long adc_T = (long) ((measurementBuf[3] & 0xFF) << 12) + (long) ((measurementBuf[4] & 0xFF) << 4) + (long) (measurementBuf[5] & 0xFF);
        long adc_P = (long) ((measurementBuf[0] & 0xFF) << 12) + (long) ((measurementBuf[1] & 0xFF) << 4) + (long) (measurementBuf[2] & 0xFF);

        double T;
        int t_fine;
        {
            // Temperature
            double var1 = (((double) adc_T) / 16384.0 - ((double) dig_t1) / 1024.0) * ((double) dig_t2);
            double var2 = ((((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0) *
                    (((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0)) * ((double) dig_t3);
            t_fine = (int) (var1 + var2);
            T = (var1 + var2) / 5120.0;
        }
        // Pressure
        double P;
        {
            double var1 = ((double) t_fine / 2.0) - 64000.0;
            double var2 = var1 * var1 * ((double) dig_p6) / 32768.0;
            var2 = var2 + var1 * ((double) dig_p5) * 2.0;
            var2 = (var2 / 4.0) + (((double) dig_p4) * 65536.0);
            var1 = (((double) dig_p3) * var1 * var1 / 524288.0 + ((double) dig_p2) * var1) / 524288.0;
            var1 = (1.0 + var1 / 32768.0) * ((double) dig_p1);
            if (var1 == 0.0) {
                P = 0;   // // avoid exception caused by division by zero
            } else {
                P = 1048576.0 - (double) adc_P;
                P = (P - (var2 / 4096.0)) * 6250.0 / var1;
                var1 = ((double) dig_p9) * P * P / 2147483648.0;
                var2 = P * ((double) dig_p8) / 32768.0;
                P = P + (var1 + var2 + ((double) dig_p7)) / 16.0;
            }
        }

        double measuredHumidity = Double.NaN;
        if (sensorType == SensorType.BME280) {
            // Humidity

            int adc_H = ((measurementBuf[6] & 0xFF) << 8) | (measurementBuf[7] & 0xFF);


            System.out.println("Raw humidity: " + adc_H / 1024.0);

            int var1 = t_fine - 76800;
            int var2 = adc_H * 16384;
            int var3 = dig_h4 * 1048576;
            int var4 = dig_h5 * var1;
            int var5 = (((var2 - var3) - var4) + 16384) / 32768;
            var2 = (var1 * dig_h6) / 1024;
            var3 = (var1 * dig_h3) / 2048;
            var4 = ((var2 * (var3 + 32768)) / 1024) + 2097152;
            var2 = ((var4 * dig_h2) + 8192) / 16384;
            var3 = var5 * var2;
            var4 = ((var3 / 32768) * (var3 / 32768)) / 128;
            var5 = var3 - ((var4 * dig_h1) / 16);
            var5 = (var5 < 0 ? 0 : var5);
            var5 = (var5 > 419430400 ? 419430400 : var5);
            double humidity = (var5 / 4096.0) / 1024.0;


/*
            double var_H = (((double)t_fine) - 76800.0);
            var_H = (adc_H - (((double)dig_h4) * 64.0 + ((double)dig_h5) / 16384.0 * var_H)) *
            (((double)dig_h2) / 65536.0 * (1.0 + ((double)dig_h6) / 67108864.0 * var_H *
                    (1.0 + ((double)dig_h3) / 67108864.0 * var_H)));
            double humidity = var_H * (1.0 - ((double)dig_h1) * var_H / 524288.0);


            var1 = t_fine - 76800.0;
            var2 = (((double) dig_h4) * 64.0 + (((double) dig_h5) / 16384.0) * var1);
            double var3 = adc_H - var2;
            double var4 = dig_h2 / 65536.0;
            double var5 = (1.0 + (dig_h3 / 67108864.0) * var1);
            double var6 = 1.0 + (dig_h6 / 67108864.0) * var1 * var5;
            var6 = var3 * var4 * (var5 * var6);
            double humidity = var6 * (1.0 - dig_h1 * var6 / 524288.0);

            int v_x1_u32r = t_fine - 76800;
            v_x1_u32r = (((((adc_H << 14) - (((int)dig_h4) << 20) - (((int)dig_h5) *
                    v_x1_u32r)) + ((int)16384)) >> 15) * (((((((v_x1_u32r *   ((int)dig_h6)) >> 10) * (((v_x1_u32r * ((int)dig_h3)) >> 11) +
                    ((int)32768))) >> 10) + ((int)2097152)) * ((int)dig_h2) +
                    8192) >> 14));
            v_x1_u32r = (v_x1_u32r - (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) *   ((int)dig_h1)) >> 4));
            v_x1_u32r = (v_x1_u32r < 0 ? 0 : v_x1_u32r);
            v_x1_u32r = (v_x1_u32r > 419430400 ? 419430400 : v_x1_u32r);
            measuredHumidity = (v_x1_u32r>>12);
        }
       */
            if (humidity > 100.0) {
                measuredHumidity = 100.0;
            } else if (humidity < 0.0) {
                measuredHumidity = 0.0;
            } else {
                measuredHumidity = humidity;
            }
        }

        return new Bmx280Measurement(T, P, measuredHumidity);
    }

    /**
     * Write the reset command to the BMP280, Sleep 100 ms
     * to allow the chip to complete the reset
     */
    public void reset() {
        writeU8Register(Bmp280Constants.RESET, Bmp280Constants.RESET_CMD);
        sleepUntil = System.currentTimeMillis() + 100;
    }

    // Internal methods

    private void materializeSleep() {
        while (true) {
            long now = System.currentTimeMillis();
            if (now >= sleepUntil) {
                return;
            }
            try {
                Thread.sleep(sleepUntil - now);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public SensorType getSensorType() {
        return sensorType;
    }


    public enum MeasurementMode {
        SLEEPING, CONTINUOUS, SINGLE
    }

    public enum SensorType {
        BME280, BMP280
    }

    private int readU8Register(int register) {
        return registerAccess.readRegister(register);
    }

    private int readS8Register(int register) {
        int unsigned = readU8Register(register);
        return unsigned > 128 ? unsigned | 0xffff_fff0 : unsigned;
    }

    private int readRegister(int register, byte[] result) {
        return registerAccess.readRegister(register, result);
    }

    private int writeU8Register(int register, int data) {
        return registerAccess.writeRegister(register, data);
    }


    final int readS16Register(int register) {
        int count = readRegister(register, ioBuf);
        if (count != 2) {
            throw new IllegalStateException("Expected two bytes reading register "+ register +"; received: " + count);
        }
        return (ioBuf[0] & 0xff) | (ioBuf[1] << 8);
    }

    final int readU16Register(int register) {
        return readS16Register(register) & 0xFFFF;
    }


    static class SpiRegisterAccess implements I2CRegisterDataReaderWriter {
        private final Spi spi;

        public SpiRegisterAccess(Spi spi) {
            this.spi = spi;
        }

        @Override
        public int readRegister(int register) {
            spi.write((byte) (0b10000000 | register));
            byte rval = this.spi.readByte();
            return rval;
        }


        @Override
        public int readRegister(byte[] bytes, byte[] bytes1, int i, int i1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int readRegister(int register, byte[] buffer, int i1, int i2) {
            this.spi.write((byte) (0b10000000 | register));
            int bytesRead = spi.read(buffer, i1, i2);

            return bytesRead;
        }


        @Override
        public int writeRegister(int register, byte data) {
            // send read request to BMP chip via SPI channel
            return spi.write((byte) (0b01111111 & register), data);
        }


        @Override
        public int writeRegister(int i, byte[] bytes, int i1, int i2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int writeRegister(byte[] bytes, byte[] bytes1, int i, int i1) {
            throw new UnsupportedOperationException();
        }
    }


}
