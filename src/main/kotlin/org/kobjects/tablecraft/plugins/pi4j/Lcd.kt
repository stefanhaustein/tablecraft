package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.display.lcd.LcdDriver
import org.kobjects.tablecraft.pluginapi.*
import kotlin.math.min

class Lcd(
    val plugin: Pi4jPlugin,
    bus: Int,
    address: Int,
    val width: Int,
    val height: Int,
    val format: List<Int?>?,
)  : OutputPortInstance {

    val i2c: I2C = plugin.pi4J.create(
        I2C.newConfigBuilder(plugin.pi4J)
            .bus(bus)
            .device(address)
            .provider("linuxfs-i2c")
            .build()
    )
    val lcdDriver = LcdDriver.create(i2c, height, width)

    override fun setValue(value: Any?) {

        val range = value as RangeValues
        for (row in 0 until min(range.height, height)) {
            var sb = StringBuilder()
            for (col in 0 until range.width) {
                val columnWidth = format?.getOrNull(col) ?: ((width - sb.length) / (range.width - col))
                val value = range[col, row]
                val s = if (value is Number)
                    Format.formatNumber(value.toDouble(), columnWidth).padStart(columnWidth)
                    else value.toString().take(columnWidth).padEnd(columnWidth)
                sb.append(s)
            }
            lcdDriver.setCursorPosition(row, 0)
            lcdDriver.write(sb.toString())
        }
    }


    override fun detach() {

            lcdDriver.close()
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
                    "format",
                    Type.STRING,
                    null,
                    setOf(ParameterSpec.Modifier.OPTIONAL)
                )
            )
        ) {
            Lcd(
                plugin,
                it["bus"] as Int,
                it["address"] as Int,
                it["width"] as Int,
                it["height"] as Int,
                (it["format"] as? String)?.split(",")?.map { it.toIntOrNull() })
        }
    }
}