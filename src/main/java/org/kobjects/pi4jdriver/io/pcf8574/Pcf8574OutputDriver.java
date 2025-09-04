package org.kobjects.pi4jdriver.io.pcf8574;

import com.pi4j.io.OnOffWrite;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.i2c.I2C;

/**
 * As the input and output functionality of this chip uses separate addresses, it seemed most straightforward
 * to implement these as separate classes.
 */
public class Pcf8574OutputDriver {
    /** PCF8574 and HLF8574 support a range of 8 addresses starting from 0x20 */
    public static final int PCF8574_ADDRESS_BASE = 0x20;

    /** PCF8574A supports a range of 8 addresses starting from 0x38 */
    public static final int PCF8574A_ADDRESS_BASE = 0x38;

    /** PCF8574T supports 8 addresses starting from 0x40 in increments of 2. */
    public static final int PCF8574T_ADDRESS_BASE = 0x40;  // Odd addresses used for input

    private final I2C i2c;
    private final OnOffWrite<?>[] outputs = new OnOffWrite[8];

    // At power on, the I/Os are high.
    private int outputBits = 0xff;
    private int triggerMask = -1;

    public Pcf8574OutputDriver(I2C i2c) {
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
    public void setTriggerMask(int mask) {
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
