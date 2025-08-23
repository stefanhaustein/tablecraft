package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.gpio.digital.DigitalOutput;

public class Parallel4BitConnection extends AbstractConnection {

    private final DigitalOutput registerSelect;
    private final DigitalOutput enable;
    private final DigitalOutput backLight;
    private final DigitalOutput d4;
    private final DigitalOutput d5;
    private final DigitalOutput d6;
    private final DigitalOutput d7;

    public Parallel4BitConnection(
            DigitalOutput registerSelect,
            DigitalOutput enable,
            DigitalOutput backLight,
            DigitalOutput d4,
            DigitalOutput d5,
            DigitalOutput d6,
            DigitalOutput d7) {
        this.registerSelect = registerSelect;
        this.enable = enable;
        this.backLight = backLight;
        this.d4 = d4;
        this.d5 = d5;
        this.d6 = d6;
        this.d7 = d7;

        // Bring the display into a well-defined 4 bit state
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x03);
        delay(35);
        sendValue(false, 0x02);
    }

    @Override
    protected void setBacklight(boolean on) {
        backLight.setState(on);
    }

    @Override
    public void sendValue(boolean registerSelect, int value) {

        this.registerSelect.setState(registerSelect);
        d4.setState((value & 0b0001_0000) != 0);
        d5.setState((value & 0b0010_0000) != 0);
        d6.setState((value & 0b0100_0000) != 0);
        d7.setState((value & 0b1000_0000) != 0);

        enable.setState(true);
        delay(1);
        enable.setState(false);
        delay(1);

        d4.setState((value & 0b0001) != 0);
        d5.setState((value & 0b0010) != 0);
        d6.setState((value & 0b0100) != 0);
        d7.setState((value & 0b1000) != 0);

        enable.setState(true);
        delay(1);
        enable.setState(false);
        delay(1);
    }
}
