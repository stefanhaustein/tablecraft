package org.kobjects.pi4jdriver.io.pcf8574;

import com.pi4j.io.OnOffRead;
import com.pi4j.io.i2c.I2C;

import java.time.Duration;
import java.time.Instant;

/**
 * As the input and output functionality of this chip uses separate addresses, it seemed most straightforward
 * to implement these as separate classes.
 */
public class Pcf8574InputDriver {

    private final I2C i2c;
    private final OnOffRead<?>[] inputs = new OnOffRead[8];

    private int lastInput;
    private Instant nextTime;
    private Duration updateInterval;

    public Pcf8574InputDriver(I2C i2c) {
        this.i2c = i2c;
        for (int i = 0; i < 8; i++) {
            final int bitIndex = i;
            inputs[i] = new OnOffRead<Object>() {
                @Override
                public boolean isOn() {
                    return (getInput() & bitIndex) != 0;
                }
            };
        }
    }

    public void setUpdateInterval(Duration updateInterval) {
        this.updateInterval = updateInterval;
    }

    public int getInput() {
        if (updateInterval == null) {
            lastInput = i2c.readByte();
        } else {
            Instant now = Instant.now();
            if (now.isAfter(nextTime)) {
                lastInput = i2c.readByte();
                nextTime = now.plus(updateInterval);
            }
        }
        return lastInput;
    }

    /**
     * Reading this input will read the corresponding input pin of the chip. This allows handing a "pin" instance to
     * other drivers.
     */
    public OnOffRead<?> getInput(int bitIndex) {
        return inputs[bitIndex];
    }

}
