/*
 *  **********************************************************************
 *  ORGANIZATION  :  Pi4J
 *  PROJECT       :  Pi4J ::  Providers
 *  FILENAME      :  BMP280Device.java
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
 *  #L%
 *
 */

package org.kobjects.pi4jdriver.sensor.environment.bmx280;


import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.spi.Spi;

/**
 * Implementation of BMP280 a Temperature/Pressure Sensor.
 */
public class Bmx280Driver {
    private final AbstractConnection connection;
    private final SensorType sensorType;


    private MeasurementMode measurementMode = MeasurementMode.SLEEPING;

    private long sleepUntil = 0;

    // Calibration values for humidity, pressure and temperature
    private final int dig_h1, dig_h2, dig_h3, dig_h4, dig_h5, dig_h6;
    private final int dig_p1, dig_p2, dig_p3, dig_p4, dig_p5, dig_p6, dig_p7, dig_p8, dig_p9;
    private final int dig_t1, dig_t2, dig_t3;


    private final byte[] measurementBuf;

    public SensorType getSensorType() {
        return sensorType;
    }


    public enum MeasurementMode {
            SLEEPING, CONTINUOUS, SINGLE
    }

    public enum SensorType {
        BME280, BMP280
    }

    public static Bmx280Driver create(I2C i2c) {
        return new Bmx280Driver(new I2cConnection(i2c));
    }

    public static Bmx280Driver create(Spi spi, DigitalOutput gcPin) {
        return new Bmx280Driver(new SpiConnection(spi, gcPin));
    }

    Bmx280Driver(AbstractConnection connection) {
        this.connection = connection;

        // read 0xD0 validate data equal 0x58 or 0x60
        int id = connection.readU8Register(Bmp280Constants.CHIP_ID);
        //SensorType sensorType;
        if (id == Bmp280Constants.ID_VALUE_MSK_BMP) {
            sensorType = SensorType.BMP280;
            measurementBuf = new byte[6];
            dig_h1 = dig_h2 = dig_h3 = dig_h4 = dig_h5 = dig_h6 = 0;

        } else if (id == Bmp280Constants.ID_VALUE_MSK_BME) {
            sensorType = SensorType.BME280;
            measurementBuf = new byte[8];

            dig_h1 = connection.readU8Register(Bme280Constants.REG_DIG_H1);
            dig_h2 = connection.readS16Register(Bme280Constants.REG_DIG_H2);
            dig_h3 = connection.readU8Register(Bme280Constants.REG_DIG_H3);
            dig_h4 = connection.readS16Register(Bme280Constants.REG_DIG_H4);
            dig_h5 = connection.readS16Register(Bme280Constants.REG_DIG_H5);
            dig_h6 = connection.readS8Register(Bme280Constants.REG_DIG_H6);
        } else {
            throw new IllegalStateException("Incorrect chip ID read");
        }

        // Read calibaration values.

        dig_t1 = connection.readU16Register(Bmp280Constants.REG_DIG_T1);
        dig_t2 = connection.readS16Register(Bmp280Constants.REG_DIG_T2);
        dig_t3 = connection.readS16Register(Bmp280Constants.REG_DIG_T3);

        dig_p1 = connection.readU16Register(Bmp280Constants.REG_DIG_P1);
        dig_p2 = connection.readS16Register(Bmp280Constants.REG_DIG_P2);
        dig_p3 = connection.readS16Register(Bmp280Constants.REG_DIG_P3);
        dig_p4 = connection.readS16Register(Bmp280Constants.REG_DIG_P4);
        dig_p5 = connection.readS16Register(Bmp280Constants.REG_DIG_P5);
        dig_p6 = connection.readS16Register(Bmp280Constants.REG_DIG_P6);
        dig_p7 = connection.readS16Register(Bmp280Constants.REG_DIG_P7);
        dig_p8 = connection.readS16Register(Bmp280Constants.REG_DIG_P8);
        dig_p9 = connection.readS16Register(Bmp280Constants.REG_DIG_P9);
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
            int ctlHum = connection.readU8Register(Bme280Constants.CTRL_HUM);
            ctlHum = (ctlHum & ~Bme280Constants.CTRL_HUM_MSK) | Bme280Constants.CTRL_HUM_SAMP_1;
            connection.writeU8Register(Bme280Constants.CTRL_HUM, ctlHum);
        }

        int ctlReg = 0; // bus.readU8Register(Bmp280Constants.CTRL_MEAS);
        ctlReg |= Bmp280Constants.CTL_FORCED;
        //ctlReg &= ~Bmp280Constants.TEMP_OVER_SAMPLE_MSK;   // mask off all temperature bits
        ctlReg |= Bmp280Constants.CTL_TEMP_SAMP_1;      // Temperature oversample 1
        //ctlReg &= ~Bmp280Constants.PRES_OVER_SAMPLE_MSK;   // mask off all pressure bits
        ctlReg |= Bmp280Constants.CTL_PRESS_SAMP_1;   //  Pressure oversample 1

        connection.writeU8Register(Bmp280Constants.CTRL_MEAS,  ctlReg);

        measurementMode = MeasurementMode.SINGLE;
        sleepUntil = System.currentTimeMillis() + 300;
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
        connection.readRegister(Bmp280Constants.PRESS_MSB, measurementBuf);

        long adc_T = (long) ((measurementBuf[3] & 0xFF) << 12) + (long) ((measurementBuf[4] & 0xFF) << 4) + (long) (measurementBuf[5] & 0xFF);
        long adc_P = (long) ((measurementBuf[0] & 0xFF) << 12) + (long) ((measurementBuf[1] & 0xFF) << 4) + (long) (measurementBuf[2] & 0xFF);

        // Temperature
        double var1 = (((double) adc_T) / 16384.0 - ((double) dig_t1) / 1024.0) * ((double) dig_t2);
        double var2 = ((((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0) *
            (((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0)) * ((double) dig_t3);
        int t_fine = (int) (var1 + var2);
        double T = (var1 + var2) / 5120.0;

        // Pressure
        double P;
        var1 = ((double) t_fine / 2.0) - 64000.0;
        var2 = var1 * var1 * ((double) dig_p6) / 32768.0;
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

        double measuredHumidity = Double.NaN;
        if (sensorType == SensorType.BME280) {
            // Humidity

            int adc_H = ((measurementBuf[6] & 0xFF) << 8) | (measurementBuf[7] & 0xFF);


            System.out.println("Raw humidity: " + adc_H / 1024.0);


            var1 = t_fine - 76800.0;
            var2 = (((double) dig_h4) * 64.0 + (((double) dig_h5) / 16384.0) * var1);
            double var3 = adc_H - var2;
            double var4 = dig_h2 / 65536.0;
            double var5 = (1.0 + (dig_h3 / 67108864.0) * var1);
            double var6 = 1.0 + (dig_h6 / 67108864.0) * var1 * var5;
            var6 = var3 * var4 * (var5 * var6);
            double humidity = var6 * (1.0 - dig_h1 * var6 / 524288.0);
 /*
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
        connection.writeU8Register(Bmp280Constants.RESET, Bmp280Constants.RESET_CMD);
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

}
