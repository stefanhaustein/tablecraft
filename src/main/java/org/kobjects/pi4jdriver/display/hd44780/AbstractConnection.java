package org.kobjects.pi4jdriver.display.hd44780;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Connection Abstraction for the Hd44780 driver. */
public abstract class AbstractConnection {
    protected abstract void setBacklight(boolean on);
    protected abstract void sendValue(Mode mode, int value);
    protected abstract boolean is8Bit();

    private Instant busyUntil = Instant.now();

    // Placing this here allows coordination of chip and connection based delays without needing a driver
    // reference here or complex interactions.
    void setDelayMicros(int micros) {
        Instant target = Instant.now().plusNanos(micros * 1000L);
        if (target.isAfter(busyUntil)) {
            busyUntil = target;
        }
    }

    void materializeDelay() {
        while (true) {
            long remaining = Instant.now().until(busyUntil, ChronoUnit.NANOS);
            if (remaining < 0) {
                break;
            }
            try {
                Thread.sleep(remaining / 1_000_000, (int) (remaining % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    public enum Mode {
        /** Sending a command */
        COMMAND,
        /** Sending a 8-bit data value */
        DATA,
        /** Sending initialization values. This will be limited to 4 bit in 4-bit mode. */
        INIT,
    }
}
