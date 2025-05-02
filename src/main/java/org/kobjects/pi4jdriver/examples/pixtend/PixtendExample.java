package org.kobjects.pi4jdriver.examples.pixtend;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.pixtend.Pixtend;

public class PixtendExample {


    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        Pixtend pixtend = new Pixtend(pi4J, Pixtend.Model.S);

        pixtend.sync();
    }

}
