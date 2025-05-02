package org.kobjects.pi4jdriver.plc.pixtend;

// Enumeration of supported Pixtend models, containing their capabilities and IO record structure.
public enum Model {
    V2S(    /* modelOut */ 'S',
            /* bufferSize */ 67,
            /* digitalInOffset */ 9,
            /* digitalInCount */ 8,
            /* analogInOffset */ 10,
            /* analogInCount */ 2,
            /* gpioInOffset */ 14,
            /* gpioCount */ 4,
            /* tempHumidOffset */ 15,
            /* tempHumidCount */ 4,
            /* retainDataOffset */ 33,
            /* retainDataCount */ 32,
            /* digitalInDebounceOffset */ 9,
            /* digitalOutOffset */ 13,
            /* digitalOutCount */ 4,
            /* relayOutOffset */ 14,
            /* relayOutCount */ 4,
            /* gpioCtrlOffset */ 15,
            /* gpioOutOffset */ 16,
            /* gpioDebounceOffset */ 17,
            /* pwmOffset */ 19,
            /* pwmCount */ 2),

    V2L(    /* modelOut */ 'L',
            /* bufferSized */ 111,
            /* digitalInOffset */ 9,
            /* digitalInCount */ 16,
            /* analogInOffset */ 11,
            /* analogInCount */ 6,
            /* gpioInOffset */ 23,
            /* gpioCount */ 4,
            /* tempHumidOffset */ 24,
            /* tempHumidCount */ 4,
            /* retainDataOffset */ 45,
            /* retainDataCount */ 64,
            /* digitalInDebounceOffset */ 8,
            /* digitalOutOffset */ 17,
            /* digitalOutCount */ 12,
            /* relayOutOffset */ 19,
            /* relayOutCount */ 4,
            /* gpioCtrlOffset */ 20,
            /* gpioOutOffset */ 21,
            /* gpioDebounceOffset */ 22,
            /* pwmOffset */ 24,
            /* pwmCount */ 3);

    public final char modelOut;
    public final int bufferSize;
    public final int digitalInOffset;
    public final int digitalInCount;
    public final int analogInOffset;
    public final int analogInCount;
    public final int gpioInOffset;
    public final int gpioCount;
    public final int tempHumidOffset;
    public final int tempHumidCount;
    public final int retainDataOffset;
    public final int retainDataCount;
    public final int digitalInDebounceOffset;
    public final int digitalOutOffset;
    public final int digitalOutCount;
    public final int relayOutOffset;
    public final int relayOutCount;
    public final int gpioCtrlOffset;
    public final int gpioOutOffset;
    public final int gpioDebounceOffset;
    public final int pwmOffset;
    public final int pwmCount;

    Model(char modelOut,
          int bufferSize,
          int digitalInOffset,
          int digitalInCount,
          int analogInOffset,
          int analogInCount,
          int gpioInOffset,
          int gpioCount,
          int tempHumidOffset,
          int tempHumidCount,
          int retainDataOffset,
          int retainDataCount,
          int digitalInDebounceOffset,
          int digitalOutOffset,
          int digitalOutCount,
          int relayOutOffset,
          int relayOutCount,
          int gpioCtrlOffset,
          int gpioOutOffset,
          int gpioDebounceOffset,
          int pwmOffset,
          int pwmCount) {
        this.modelOut = modelOut;
        this.bufferSize = bufferSize;
        this.digitalInOffset = digitalInOffset;
        this.digitalInCount = digitalInCount;
        this.analogInOffset = analogInOffset;
        this.analogInCount = analogInCount;
        this.gpioInOffset = gpioInOffset;
        this.gpioCount = gpioCount;
        this.tempHumidOffset = tempHumidOffset;
        this.tempHumidCount = tempHumidCount;
        this.retainDataOffset = retainDataOffset;
        this.retainDataCount = retainDataCount;
        this.digitalInDebounceOffset = digitalInDebounceOffset;
        this.digitalOutOffset = digitalOutOffset;
        this.digitalOutCount = digitalOutCount;
        this.relayOutOffset = relayOutOffset;
        this.relayOutCount = relayOutCount;
        this.gpioCtrlOffset = gpioCtrlOffset;
        this.gpioOutOffset = gpioOutOffset;
        this.gpioDebounceOffset = gpioDebounceOffset;
        this.pwmOffset = pwmOffset;
        this.pwmCount = pwmCount;
    }
}
