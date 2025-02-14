package freenove;

class PCF8574 {
    private int address;
    private IIC i2c;
    private int currValue;

    public PCF8574(int addr) {
        address = addr;
        i2c = new IIC(IIC.list()[0]);
        currValue = 0;
    }

    public int digitalRead(int pin) {
        int val = readByte();
        return ((val & (1 << pin)) == (1 << pin)) ? 1 : 0;
    }

    public int readByte() {
        return currValue;
    }

    public void digitalWrite(int pin, int val) {
        int value = currValue;
        if (val == 1) {
            value |= (1 << pin);
        } else if (val == 0) {
            value &= ~(1 << pin);
        } else {
            return;
        }
        writeByte(value);
    }

    public void writeByte(int data) {
        currValue = data;
        i2c.beginTransmission(address);
        i2c.write(data);
        i2c.endTransmission();
    }

    public int getCurrentValue() {
        return currValue;
    }
}
