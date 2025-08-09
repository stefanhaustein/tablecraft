package org.kobjects.pi4jdriver.examples.plc.pixtend;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.plc.pixtend.Model;
import org.kobjects.pi4jdriver.plc.pixtend.PixtendDriver;

public class PixtendExample {


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        PixtendDriver pixtend = new PixtendDriver(pi4J, Model.V2S);

        pixtend.syncState();

        for (int i = 0; i < pixtend.model.digitalInCount; i++) {
            System.out.println("Digital input " + i + ": " + pixtend.getDigitalIn(i));
        }
        for (int i = 0; i < pixtend.model.analogInCount; i++) {
            System.out.println("Analog input " + i + ": " + pixtend.getAnalogIn(i));
        }
        for (int i = 0; i < pixtend.model.gpioCount; i++) {
            System.out.println("GPIO in " + i + ": " + pixtend.getGpioIn(i));
        }
    }

}
