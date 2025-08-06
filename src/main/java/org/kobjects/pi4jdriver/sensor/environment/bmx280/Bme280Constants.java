package org.kobjects.pi4jdriver.sensor.environment.bmx280;

/** Additional constants for the BME280 */
class Bme280Constants {
    static final int REG_DIG_H1 = 0xA1;
    static final int REG_DIG_H2 = 0xE1;
    static final int REG_DIG_H3 = 0xE3;
    static final int REG_DIG_H4 = 0xE4;
    static final int REG_DIG_H5 = 0xE5;
    static final int REG_DIG_H6 = 0xE7;

    static final int CTRL_HUM = 0xF2;
    static final int HUM_MSB = 0xFD;
    static final int HUM_LSB = 0xFE;

    static int CTRL_HUM_SAMP_1 = 0x01;
    static int CTRL_HUM_MSK = 7;
}
