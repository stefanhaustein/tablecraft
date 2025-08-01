package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.tablecraft.pluginapi.*

class DigitalOutputPort(
    val plugin: Pi4jPlugin,
    val address: Int
)  : OutputPortInstance {

    val digitalOutput= plugin.createDigitalOutput(DigitalOutputConfig.newBuilder(plugin.pi4J).address(address).build())



    override fun setValue(value: Any?) {
        val value = when(val raw = value) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw;")
        }
        digitalOutput.setState(value)
    }

    override fun detach() {
        plugin.releasePort(address, digitalOutput)
    }


    companion object {
        fun spec(plugin: Pi4jPlugin) = OutputPortSpec(
            "GPIO",
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address", Type.INT, null, setOf(ParameterSpec.Modifier.CONSTANT))),
        ) { DigitalOutputPort(plugin, it["address"] as Int) }
    }
}