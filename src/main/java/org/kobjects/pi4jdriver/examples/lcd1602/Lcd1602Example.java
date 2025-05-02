package org.kobjects.pi4jdriver.examples.lcd1602;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.kobjects.pi4jdriver.lcd1602.Lcd1602;

public class Lcd1602Example {

    public static void main(String[] args) {
        Context pi4J = Pi4J.newAutoContext();

        Lcd1602 lcd1602 = new Lcd1602(pi4J);

        lcd1602.clearDisplay();
        lcd1602.displayText("Hello World!");
    }
}
