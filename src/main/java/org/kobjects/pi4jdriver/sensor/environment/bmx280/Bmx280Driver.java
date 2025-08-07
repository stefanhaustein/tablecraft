/*
 * Copyright (C) 2012 - 2022 Pi4J
 * Copyright (C) 2025 Stefan Haustein
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

package org.kobjects.pi4jdriver.sensor.environment.bmx280;

import com.pi4j.io.i2c.I2CRegisterDataReaderWriter;
import com.pi4j.io.spi.Spi;

/**
 * Driver for BME 280 and BMP 280 chips.
 */
public class Bmx280Driver {

    private final I2CRegisterDataReaderWriter registerAccess;
    private final SensorType sensorType;
    private MeasurementMode measurementMode = MeasurementMode.SLEEPING;

    private long sleepUntil = 0;
    // Calibration values for humidity, pressure and temperature
    private final double digH1, digH2, digH3, digH4, digH5, digH6;
    private final double digP1, digP2, digP3, digP4, digP5, digP6, digP7, digP8, digP9;
    private final double digT1, digT2, digT3;

    private final byte[] measurementBuf;
    private final byte[] ioBuf = new byte[2];

    private SensorMode temperatureMode = SensorMode.ENABLED;
    private SensorMode pressureMode = SensorMode.ENABLED;
    private SensorMode humidityMode;

    public Bmx280Driver(Spi spi) {
        this (new SpiRegisterAccess(spi));
    }

    public Bmx280Driver(I2CRegisterDataReaderWriter registerAccess) {
        this.registerAccess = registerAccess;

        int id = readU8Register(Bmp280Constants.CHIP_ID);
        if (id == Bmp280Constants.ID_VALUE_BMP) {
            sensorType = SensorType.BMP280;
            measurementBuf = new byte[6];
            digH1 = digH2 = digH3 = digH4 = digH5 = digH6 = 0;
            humidityMode = SensorMode.DISABLED;

        } else if (id == Bmp280Constants.ID_VALUE_BME) {
            sensorType = SensorType.BME280;
            measurementBuf = new byte[8];

            digH1 = readU8Register(Bme280Constants.REG_DIG_H1);
            digH2 = readS16Register(Bme280Constants.REG_DIG_H2);
            digH3 = readU8Register(Bme280Constants.REG_DIG_H3);

            int e4 = readU8Register(0xe4);
            int e5 = readU8Register(0xe5);

            int h4Hsb = e4 * 16;
            int h4Lsb = e5 & 0x0f;
            digH4 = h4Hsb | h4Lsb;

            int e6 = readU8Register(0xe6);

            int h5Lsb = e5 >> 4;
            int h5Hsb = e6 * 16;
            digH5 = h5Hsb | h5Lsb;

            digH6 = readS8Register(Bme280Constants.REG_DIG_H6);
            humidityMode = SensorMode.ENABLED;

        } else {
            throw new IllegalStateException("Unrecognized chip ID: " + id);
        }

        // Read calibration values.

        digT1 = readU16Register(Bmp280Constants.REG_DIG_T1);
        digT2 = readS16Register(Bmp280Constants.REG_DIG_T2);
        digT3 = readS16Register(Bmp280Constants.REG_DIG_T3);

        digP1 = readU16Register(Bmp280Constants.REG_DIG_P1);
        digP2 = readS16Register(Bmp280Constants.REG_DIG_P2);
        digP3 = readS16Register(Bmp280Constants.REG_DIG_P3);
        digP4 = readS16Register(Bmp280Constants.REG_DIG_P4);
        digP5 = readS16Register(Bmp280Constants.REG_DIG_P5);
        digP6 = readS16Register(Bmp280Constants.REG_DIG_P6);
        digP7 = readS16Register(Bmp280Constants.REG_DIG_P7);
        digP8 = readS16Register(Bmp280Constants.REG_DIG_P8);
        digP9 = readS16Register(Bmp280Constants.REG_DIG_P9);
    }

    /**
     * Configure the BMP/E280 measurement mode.
     */
    public void setMeasurementMode(MeasurementMode mode) {
        if (measurementMode != mode) {
            measurementMode = mode;
            updateConfiguration();
        }
    }

    private void updateConfiguration() {
        // set forced mode to leave sleep mode state and initiate measurements.
        // At measurement completion chip returns to sleep mode

        if (sensorType == SensorType.BME280) {
            int ctlHum = readU8Register(Bme280Constants.CTRL_HUM);
            ctlHum = (ctlHum & ~Bme280Constants.CTRL_HUM_MSK) | humidityMode.ordinal();
            writeU8Register(Bme280Constants.CTRL_HUM, ctlHum);
        }

        int ctlReg = Bmp280Constants.POWERMODE_FORCED
            | (temperatureMode.ordinal() << Bmp280Constants.CTRL_TEMP_POS)
            | (pressureMode.ordinal() << Bmp280Constants.CTRL_PRESS_POS);

        writeU8Register(Bmp280Constants.CTRL_MEAS, ctlReg);

        sleepUntil = System.currentTimeMillis() + 1000;
    }

    public void setTemperatureMode(SensorMode mode) {
        if (this.temperatureMode != mode) {
            this.temperatureMode = mode;
            if (measurementMode != MeasurementMode.SLEEPING) {
                updateConfiguration();
            }
        }
    }

    public void setPressureMode(SensorMode mode) {
        if (this.pressureMode != mode) {
            this.pressureMode = mode;
            if (measurementMode != MeasurementMode.SLEEPING) {
                updateConfiguration();
            }
        }
    }

    public void setHumidityMode(SensorMode mode) {
        if (this.humidityMode != mode) {
            this.humidityMode = mode;
            if (measurementMode != MeasurementMode.SLEEPING) {
                updateConfiguration();
            }
        }
    }

    /**
     * Read measure registers 0xF7 - 0xFC in single read to ensure all the data pertains to
     * a single  measurement.
     * <p>
     * Use conversion data and measure data to calculate temperature in C and pressure in Pa.
     */
    public Measurement readMeasurements() {
        if (measurementMode == MeasurementMode.SLEEPING) {
            setMeasurementMode(MeasurementMode.SINGLE);
        }

        materializeSleep();

        readRegister(Bmp280Constants.PRESS_MSB, measurementBuf);

        double adcT = ((measurementBuf[3] & 0xFF) << 12) + ((measurementBuf[4] & 0xFF) << 4) + (measurementBuf[5] & 0xFF);
        double adcP = ((measurementBuf[0] & 0xFF) << 12) + ((measurementBuf[1] & 0xFF) << 4) + (measurementBuf[2] & 0xFF);

        // Temperature
        double var1 = (adcT / 16384.0 - digT1 / 1024.0) * digT2;
        double var2 = ((adcT / 131072.0 - digT1 / 8192.0) *
                    (adcT / 131072.0 - digT1 / 8192.0)) * digT3;
        double tFine = var1 + var2;
        double temperature = tFine / 5120.0;

        // Pressure
        double pressure;
        var1 = (tFine / 2.0) - 64000.0;
        var2 = var1 * var1 * digP6 / 32768.0;
        var2 = var2 + var1 * digP5 * 2.0;
        var2 = (var2 / 4.0) + (digP4 * 65536.0);
        var1 = (digP3 * var1 * var1 / 524288.0 + digP2 * var1) / 524288.0;
        var1 = (1.0 + var1 / 32768.0) * digP1;
        if (var1 == 0.0) {
            pressure = 0;   // // avoid exception caused by division by zero
        } else {
            pressure = 1048576.0 - adcP;
            pressure = (pressure - (var2 / 4096.0)) * 6250.0 / var1;
            var1 = digP9 * pressure * pressure / 2147483648.0;
            var2 = pressure * digP8 / 32768.0;
            pressure = pressure + (var1 + var2 + digP7) / 16.0;
        }

        double humidity = Double.NaN;
        double adcH = Double.NaN;
        if (sensorType == SensorType.BME280) {
            // Humidity

            adcH = ((measurementBuf[6] & 0xFF) << 8) | (measurementBuf[7] & 0xFF);

            double varH = tFine - 76800.0;
            varH = (adcH - (digH4 * 64.0 + digH5 / 16384.0 *
                    varH)) * (digH2 / 65536.0 * (1.0 + digH6 /
                    67108864.0 * varH *
                    (1.0 + digH3 / 67108864.0 * varH)));
            varH = varH * (1.0 - digH1 * varH / 524288.0);

            if (varH > 100.0) {
                varH = 100.0;
            } else if (varH < 0.0) {
                varH = 0.0;
            }
            humidity = varH;
        }

        if (measurementMode == MeasurementMode.SINGLE) {
            measurementMode = MeasurementMode.SLEEPING;
        }

        return new Measurement(
                adcT / 1024.0, adcP / 1024.0, adcH / 1024.0,
                temperature, pressure, humidity);
    }

    /**
     * Write the reset command to the BMP280, Sleep 100 ms
     * to allow the chip to complete the reset
     */
    public void reset() {
        writeU8Register(Bmp280Constants.RESET, Bmp280Constants.RESET_CMD);
        sleepUntil = System.currentTimeMillis() + 100;
    }

    public SensorType getSensorType() {
        return sensorType;
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

    private int readS16Register(int register) {
        int count = readRegister(register, ioBuf);
        if (count != 2) {
            throw new IllegalStateException("Expected two bytes reading register "+ register +"; received: " + count);
        }
        return (ioBuf[0] & 0xff) | (ioBuf[1] << 8);
    }

    private int readU16Register(int register) {
        return readS16Register(register) & 0xFFFF;
    }


    public static class Measurement {
        private final double rawTemperature;
        private final double rawPressure;
        private final double rawHumidity;
        private final double temperature;
        private final double pressure;
        private final double humidity;

        Measurement(
                double rawTemperature, double rawPressure, double rawHumidity,
                double temperature, double pressure, double humidity) {
            this.rawTemperature = rawTemperature;
            this.rawPressure = rawPressure;
            this.rawHumidity = rawHumidity;
            this.temperature = temperature;
            this.pressure = pressure;
            this.humidity = humidity;
        }

        public double getRawTemperature() {
            return rawTemperature;
        }

        public double getRawHumidity() {
            return rawHumidity;
        }

        public double getRawPressure() {
            return rawPressure;
        }

        public double getTemperature() {
            return temperature;
        }

        public double getHumidity() {
            return humidity;
        }

        public double getPressure() {
            return pressure;
        }
    }

    public enum SensorMode {
        DISABLED, ENABLED, OVERSAMPLE_1X, OVERSAMPLE_2X, OVERSAMPLE_4X, OVERSAMPLE_8X, OVERSAMPLE_16X
    }

    public enum MeasurementMode {
        SLEEPING, CONTINUOUS, SINGLE
    }

    public enum SensorType {
        BME280, BMP280
    }
}
