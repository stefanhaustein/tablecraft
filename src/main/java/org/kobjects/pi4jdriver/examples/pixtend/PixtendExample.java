package org.kobjects.pi4jdriver.examples.pixtend;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.pixtend.Model;
import org.kobjects.pi4jdriver.pixtend.Pixtend;

public class PixtendExample {


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        Pixtend pixtend = new Pixtend(Model.V2S, pi4J);

        pixtend.syncState();

        for (int i = 0; i < pixtend.model.digitalInCount; i++) {
            System.out.println("Digital input " + i + ": " + pixtend.getDigitalIn(i));
        }
        for (int i = 0; i < pixtend.model.analogInCount; i++) {
            System.out.println("Analog input " + i + ": " + pixtend.getRawAnalogIn(i));
        }
    }

}
