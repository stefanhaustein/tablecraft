package org.kobjects.pi4jdriver.sensor.environment.bmx280;

public class Bmx280Measurement {
    private final double temperature;
    private final double pressure;
    private final double humidity;

    Bmx280Measurement(double temperature, double pressure, double humidity) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
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
