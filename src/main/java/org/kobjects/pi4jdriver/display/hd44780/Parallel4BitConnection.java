package org.kobjects.pi4jdriver.display.hd44780;

import com.pi4j.io.OnOffWrite;
import com.pi4j.io.gpio.digital.DigitalOutput;

public class Parallel4BitConnection extends AbstractConnection {

    private final OnOffWrite<?> registerSelect;
    private final OnOffWrite<?> enable;
    private final OnOffWrite<?> backLight;
    private final OnOffWrite<?> d4;
    private final OnOffWrite<?> d5;
    private final OnOffWrite<?> d6;
    private final OnOffWrite<?> d7;

    private static void setState(OnOffWrite<?> pin, boolean state) {
        if (state) {
            pin.on();
        } else {
            pin.off();
        }
    }

    public Parallel4BitConnection(
            OnOffWrite<?> registerSelect,
            OnOffWrite<?> enable,
            OnOffWrite<?> backLight,
            OnOffWrite<?> d4,
            OnOffWrite<?> d5,
            OnOffWrite<?> d6,
            OnOffWrite<?> d7) {
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
        setState(backLight, on);
    }

    @Override
    public void sendValue(boolean registerSelect, int value) {

        setState(this.registerSelect, registerSelect);
        setState(d4, (value & 0b0001_0000) != 0);
        setState(d5, (value & 0b0010_0000) != 0);
        setState(d6, (value & 0b0100_0000) != 0);
        setState(d7, (value & 0b1000_0000) != 0);

        setState(enable, true);
        delay(1);
        setState(enable, false);
        delay(1);

        setState(d4, (value & 0b0001) != 0);
        setState(d5, (value & 0b0010) != 0);
        setState(d6, (value & 0b0100) != 0);
        setState(d7, (value & 0b1000) != 0);

        setState(enable, true);
        delay(1);
        setState(enable, false);
        delay(1);
    }
}
