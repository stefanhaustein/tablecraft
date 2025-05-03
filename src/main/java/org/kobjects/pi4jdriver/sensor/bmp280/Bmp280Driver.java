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

package org.kobjects.pi4jdriver.sensor.bmp280;


/**
 * Implementation of BMP280 a Temperature/Pressure Sensor.
 */
public class Bmp280Driver {
    private final Bmp280Io io;

    private double lastMeasurement;
    private double measuredPressure;
    private double measuredTemperature;

    public Bmp280Driver(Bmp280Io io) {
        this.io = io;

        resetSensor();
        // read 0xD0 validate data equal 0x58 or 0x60
        int id = io.readRegister(Bmp280Constants.CHIP_ID);
        if ((id != Bmp280Constants.ID_VALUE_MSK_BMP) && (id != Bmp280Constants.ID_VALUE_MSK_BME)) {
            throw new IllegalStateException("Incorrect chip ID read");
        }
    }

    /**
     * @param read 8 bits data
     * @return unsigned value
     */
    private int castOffSignByte(byte read) {
        return ((int) read & 0Xff);
    }

    /**
     * @param read 16 bits of data  stored in 8 bit array
     * @return 16 bit signed
     */
    private int signedInt(byte[] read) {
        int temp = (read[0] & 0xff);
        temp += (((long) read[1]) << 8);
        return temp;
    }

    /**
     * @param read 16 bits of data  stored in 8 bit array
     * @return 64 bit unsigned value
     */
    private long castOffSignInt(byte[] read) {
        long temp = ((long) read[0] & 0xff);
        temp += (((long) read[1] & 0xff)) << 8;
        return temp;
    }


    /**
     * Reset BMP280 chip to remove any previous applications configuration details.
     * <p>
     * Configure BMP280 for 1x oversamplimg and single measurement.
     * <p>
     * Read and store all factory set conversion data.
     * Read measure registers 0xf7 - 0xFC in single read to ensure all the data pertains to
     * a single  measurement.
     * <p>
     * Use conversion data and measure data to calculate temperature in C and pressure in Pa.
     *
     * Store the measured data.
     *
     * If a call is made withing 50ms of the previous call, cached values are used.
     */
    public void readBmp280() {
        if (System.currentTimeMillis() - lastMeasurement < 50) {
            return;
        }

        // set forced mode to leave sleep mode state and initiate measurements.
        // At measurement completion chip returns to sleep mode
        int ctlReg = io.readRegister(Bmp280Constants.CTRL_MEAS);
        ctlReg |= Bmp280Constants.CTL_FORCED;
        ctlReg &= ~Bmp280Constants.TEMP_OVER_SAMPLE_MSK;   // mask off all temperauire bits
        ctlReg |= Bmp280Constants.CTL_TEMP_SAMP_1;      // Temperature oversample 1
        ctlReg &= ~Bmp280Constants.PRES_OVER_SAMPLE_MSK;   // mask off all pressure bits
        ctlReg |= Bmp280Constants.CTL_PRESS_SAMP_1;   //  Pressure oversample 1

        io.writeRegister(Bmp280Constants.CTRL_MEAS, (byte) ctlReg);


        // Next delay for 100 ms to provide chip time to perform measurements
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // read the temp factory errata

        byte[] compVal = new byte[2];

        io.readRegister(Bmp280Constants.REG_DIG_T1, compVal);

        long dig_t1 = castOffSignInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_T2, compVal);
        int dig_t2 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_T3, compVal);
        int dig_t3 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P1, compVal);
        long dig_p1 = castOffSignInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P2, compVal);
        int dig_p2 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P3, compVal);
        int dig_p3 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P4, compVal);
        int dig_p4 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P5, compVal);
        int dig_p5 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P6, compVal);
        int dig_p6 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P7, compVal);
        int dig_p7 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P8, compVal);
        int dig_p8 = signedInt(compVal);

        io.readRegister(Bmp280Constants.REG_DIG_P9, compVal);
        int dig_p9 = signedInt(compVal);


        byte[] buff = new byte[6];

        io.readRegister(Bmp280Constants.PRESS_MSB, buff);


        long adc_T = (long) ((buff[3] & 0xFF) << 12) + (long) ((buff[4] & 0xFF) << 4) + (long) (buff[5] & 0xFF);

        long adc_P = (long) ((buff[0] & 0xFF) << 12) + (long) ((buff[1] & 0xFF) << 4) + (long) (buff[2] & 0xFF);

        // Temperature
        double var1 = (((double) adc_T) / 16384.0 - ((double) dig_t1) / 1024.0) * ((double) dig_t2);
        double var2 = ((((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0) *
            (((double) adc_T) / 131072.0 - ((double) dig_t1) / 8192.0)) * ((double) dig_t3);
        int t_fine = (int) (var1 + var2);
        double T = (var1 + var2) / 5120.0;


        measuredTemperature = T;
        
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
        measuredPressure = P;
        lastMeasurement = System.currentTimeMillis();
    }


    /**
     * @return Temperature centigrade
     */
    public double temperatureC() {
        readBmp280();
        return measuredTemperature;
    }

    /**
     * @return Temperature fahrenheit
     */
    public double temperatureF() {
        return temperatureC() * 1.8 + 32;
    }

    /**
     * @return Pressure in Pa units
     */
    public double pressurePa() {
        readBmp280();
        return measuredPressure;
    }

    /**
     * @return Pressure in millBar
     */
    public double pressureMb() {
        return pressurePa() / 100;
    }

    /**
     * @return Pressure in inches mercury
     */
    public double pressureIn() {
        return pressurePa() / 3386;
    }

    /**
     * Write the reset command to the BMP280, Sleep 100 ms
     * to allow the chip to complete the reset
     */
    public void resetSensor() {
        int rc = io.writeRegister(Bmp280Constants.RESET, Bmp280Constants.RESET_CMD);

        // Next delay for 100 ms to provide chip time to perform reset
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
