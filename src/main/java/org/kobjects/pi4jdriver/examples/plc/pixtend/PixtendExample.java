package org.kobjects.pi4jdriver.examples.plc.pixtend;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver;

public class PixtendExample {


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        PiXtendDriver pixtend = new PiXtendDriver(pi4J, PiXtendDriver.Model.V2S);

        pixtend.setGpioMode(0, PiXtendDriver.GpioMode.DHT22);

        for (int j = 0; j < 10; j++) {

            pixtend.syncState();

            for (int i = 0; i < pixtend.model.digitalInCount; i++) {
                System.out.println("Digital input " + i + ": " + pixtend.getDigitalIn(i));
            }
            for (int i = 0; i < pixtend.model.analogInCount; i++) {
                System.out.println("Analog input " + i + ": " + pixtend.getAnalogIn(i));
            }
            System.out.println("GPIO DHT 22 pin 0 t= " + pixtend.getTemperature(0) + " °C h= " + pixtend.getHumidity(0) + " %Rh");

            for (int i = 1; i < pixtend.model.gpioCount; i++) {
                System.out.println("GPIO in " + i + ": " + pixtend.getGpioIn(i));
            }
        }
    }

}
