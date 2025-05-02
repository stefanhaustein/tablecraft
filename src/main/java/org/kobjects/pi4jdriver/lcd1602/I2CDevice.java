/*
 * Forked from
 * https://github.com/Pi4J/pi4j-example-components/blob/main/src/main/java/com/pi4j/catalog/components/base/I2CDevice.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kobjects.pi4jdriver.lcd1602;

import java.time.Duration;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;

public abstract class I2CDevice {

    /**
     * The Default BUS and Device Address.
     * On the PI, you can look it up with the Command 'sudo i2cdetect -y 1'
     */
    protected static final int DEFAULT_BUS = 0x01;

    /**
     * The PI4J I2C component
     */
    private final I2C i2c;

    protected void delay(Duration duration) {
        try {
            long nanos = duration.toNanos();
            long millis = nanos / 1_000_000;
            int remainingNanos = (int) (nanos % 1_000_000);
            Thread.currentThread().sleep(millis, remainingNanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    protected void logInfo(String msg, Object... args) {
        System.out.printf(msg, args);
    }

    protected void logError(String msg, Object... args) {
        System.out.printf(msg, args);
    }

    protected void logDebug(String msg, Object... args) {
        System.out.printf(msg, args);
    }

    protected void logException(String msg, Throwable exception){
        System.out.printf(msg, exception);
    }
    protected I2CDevice(Context pi4j, int device, String name){
        i2c = pi4j.create(I2C.newConfigBuilder(pi4j)
                .id("I2C-" + DEFAULT_BUS + "@" + device)
                .name(name+ "@" + device)
                .bus(DEFAULT_BUS)
                .device(device)
                .build());
        init(i2c);
        logDebug("I2C device %s initialized", name);
    }


    /**
     * send a single command to device
     */
    protected void sendCommand(byte cmd) {
        i2c.write(cmd);
        delay(Duration.ofNanos(100_000));
    }

    protected int readRegister(int register) {
        return i2c.readRegisterWord(register);
    }

    /**
     * send custom configuration to device
     *
     * @param config custom configuration
     */
    protected void writeRegister(int register, int config) {
        i2c.writeRegisterWord(register, config);
    }

    /**
     * send some data to device
     *
     * @param data
     */
    protected void write(byte data){
        i2c.write(data);
    }

    /**
     * Execute Display commands
     *
     * @param command Select the LCD Command
     * @param data    Setup command data
     */
    protected void sendCommand(byte command, byte data) {
        sendCommand((byte) (command | data));
    }

    protected abstract void init(I2C i2c);

}