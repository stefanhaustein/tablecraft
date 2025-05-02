package org.kobjects.pi4jdriver.examples.pixtend;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.pixtend.Pixtend;

public class PixtendExample {


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        Pixtend pixtend = new Pixtend(Pixtend.Model.V2S, pi4J);

        pixtend.sync();

        for (int i = 0; i < pixtend.model.digitalInCount; i++) {
            System.out.println("Digital input " + i + ": " + pixtend.getDigitalIn(i));
        }
        for (int i = 0; i < pixtend.model.analogInCount; i++) {
            System.out.println("Analog input " + i + ": " + pixtend.getAnalogIn(i));
        }
    }

}
