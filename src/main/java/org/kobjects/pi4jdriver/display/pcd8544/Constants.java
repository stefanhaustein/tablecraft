package org.kobjects.pi4jdriver.display.pcd8544;

public class Constants {
    
    static final int WIDTH = 84;  ///< LCD is 84 pixels wide
    static final int HEIGHT = 48;

    static final int DEFAULT_COTRAST = 40;
    static final int DEFAULT_BIAS = 4;

            static final int POWERDOWN = 0x04;
            static final int ENTRYMODE = 0x02;
            static final int EXTENDEDINSTRUCTION = 0x01;

            public static final int DISPLAY_CONTROL = 0x08;
            public static final int DC_BLANK = 0x0;
            static final int DC_NORMAL = 0x4;
            public static final int DC_ALLON = 0x1;
            static final int DC_INVERTED = 0x5;

            static final int FUNCTIONSET = 0x20;
            static final int SETYADDR = 0x40;
            static final int SETXADDR = 0x80;

            static final int SETTEMP  =  0x04;
            static final int SETBIAS = 0x10;
            static final int SETVOP =  0x80;

}
