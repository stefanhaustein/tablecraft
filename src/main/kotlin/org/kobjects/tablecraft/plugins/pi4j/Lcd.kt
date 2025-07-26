package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.display.lcd.LcdDriver
import org.kobjects.tablecraft.pluginapi.*
import kotlin.math.min

class Lcd(
    val plugin: Pi4jPlugin,
    val bus: Int,
    val address: Int,
    val width: Int,
    val height: Int,
)  : OutputPortInstance {

    var lcdDriver: LcdDriver? = null
    var error: Exception? = null

    override fun setValue(value: Any?) {
        if (error != null) {
            throw IllegalStateException("Lcd Driver initialization error", error)
        }

        val range = value as RangeValues
        for (row in 0 until min(range.height, height)) {
            for (col in 0 until range.width) {
                val xPos = col * width / range.width
                lcdDriver?.setCursorPosition(row, xPos)
                val columnWidth = (col + 1) * width / range.width - xPos
                lcdDriver?.write(range[col, row].toString().take(columnWidth))
            }
        }

    }

    override fun attach() {
        try {
            val i2c: I2C = plugin.pi4J.create(
                I2C.newConfigBuilder(plugin.pi4J)
                    .bus(bus)
                    .device(address)
                    .provider("linuxfs-i2c")
                    .build()
            )
            lcdDriver = LcdDriver.create(i2c, height, width)
            error = null
            System.err.println("**** LCD Driver attached successfully: $lcdDriver")
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
        }
    }


    override fun detach() {
        if (lcdDriver != null) {
            lcdDriver?.close()
            lcdDriver = null
        }
    }



    companion object {
        fun spec(plugin: Pi4jPlugin) = OutputPortSpec(
            "Drivers",
            "Lcd",
            "An LCD text display",
            listOf(
                ParameterSpec(
                    "bus",
                    Type.INT,
                    1
                ),
                ParameterSpec(
                    "address",
                    Type.INT,
                    0x27
                ),
                ParameterSpec(
                    "width",
                    Type.INT,
                    16
                ),
                ParameterSpec(
                    "height",
                    Type.INT,
                    2
                ),
                ParameterSpec(
                    "column_widths",
                    Type.STRING,
                    null,
                    setOf(ParameterSpec.Modifier.OPTIONAL)
                )
        )
        ) {
            Lcd(plugin, it["bus"] as Int, it["address"] as Int, it["width"] as Int, it["height"] as Int) }
    }

}