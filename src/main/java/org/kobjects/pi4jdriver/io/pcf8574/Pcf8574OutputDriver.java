package org.kobjects.pi4jdriver.io.pcf8574;

import com.pi4j.io.OnOffWrite;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.i2c.I2C;

/**
 * As the input and output functionality of this chip uses separate addresses, it seemed most straightforward
 * to implement these as separate classes.
 */
public class Pcf8574OutputDriver {

    private final I2C i2c;
    private final OnOffWrite<?>[] outputs = new OnOffWrite[8];

    private int outputBits;
    private int triggerMask = -1;

    Pcf8574OutputDriver(I2C i2c) {
        this.i2c = i2c;
        for (int i = 0; i < 8; i++) {
            final int bitIndex = i;
            outputs[i] = new OnOffWrite<Object>() {
                @Override
                public Object on() throws IOException {
                    setState(outputBits | 1 << bitIndex);
                    return this;
                }

                @Override
                public Object off() throws IOException {
                    setState(outputBits & ~(1 << bitIndex));
                    return this;
                }
            };
        }
    }

    /**
     * Sets a mask for which bit changes trigger sending the changed state over i2c. By default,
     * all bit changes trigger an update.
     */
    void setTriggerMask(int mask) {
        this.triggerMask = mask;
    }

    /**
     * Writing to this output will set the corresponding output pin of the chip. This allows handing a pin
     * instance to other drivers.
     */
    public OnOffWrite<?> getOutput(int bitIndex) {
        return outputs[bitIndex];
    }

    /** Returns true if an update was sent. */
    public boolean setState(int bits) {
        int changedBits = outputBits ^ bits;
        outputBits = bits;
        if ((changedBits & triggerMask) != 0) {
            this.i2c.write(outputBits);
            return true;
        }
        return false;
    }
}
