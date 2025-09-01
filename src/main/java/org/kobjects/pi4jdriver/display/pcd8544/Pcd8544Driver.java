package org.kobjects.pi4jdriver.display.pcd8544;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.spi.Spi;

/** http://eia.udg.edu/~forest/PCD8544_1.pdf */
public class Pcd8544Driver {

    private final Spi spi;
    private final DigitalOutput rst;
    private final DigitalOutput dc;
    public Pcd8544Driver(Spi spi, DigitalOutput rst, DigitalOutput dc) {
        this.spi = spi;
        this.rst = rst;
        this.dc = dc;

        rst.low();
        delay(1);
        rst.high();

        setBias(Constants.DEFAULT_BIAS);
        setContrast(Constants.DEFAULT_COTRAST);

        command(Constants.FUNCTIONSET);
        command(Constants.DISPLAY_CONTROL | Constants.DC_NORMAL);

    }

    public void setContrast(int contrast) {
        if (contrast > 0x7f) {
            contrast = 0x7f;
        }
        command(Constants.FUNCTIONSET | Constants.EXTENDEDINSTRUCTION);
        command(Constants.SETVOP | contrast);
        command(Constants.FUNCTIONSET);
    }


    public void setBias(int bias) {
        if (bias > 0x7) {
            bias = 0x7;
        }
        command(Constants.FUNCTIONSET | Constants.EXTENDEDINSTRUCTION);
        command(Constants.SETBIAS | bias);
        command(Constants.FUNCTIONSET);
    }

    public void setPixels(int x, int y, byte[] pixels) {
        command(Constants.SETYADDR | y);
        command(Constants.SETXADDR | x);
        dc.high();
        spi.write(pixels);
    }


    public void command(int c) {
        dc.low();
        spi.write(c);
    }

    private void data(int d) {
        dc.high();
        spi.write(d);
    }

    private void delay(int timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
