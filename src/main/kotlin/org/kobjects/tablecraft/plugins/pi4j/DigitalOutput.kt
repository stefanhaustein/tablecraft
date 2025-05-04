package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.tablecraft.pluginapi.*

class DigitalOutput(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
)  : OutputPortInstance, Pi4JPortHolder {

    var digitalOutput: DigitalOutput? = null
    var error: Exception? = null

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (configuration["address"] as Number).toInt()
        try {
            digitalOutput = plugin.pi4J.create(DigitalOutputConfig.newBuilder(plugin.pi4J).address(address).build())
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            digitalOutput = null
        }
    }

    override fun setValue(value: Any) {
        if (error != null) {
            throw error!!
        }
        val value = when(val raw = value) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw;")
        }
        digitalOutput!!.setState(value)
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
    }

    companion object {
        fun spec(plugin: Pi4jPlugin) = OutputPortSpec(
            Type.BOOL,
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address",  Type.INT, setOf(ParameterSpec.Modifier.CONSTANT))),
        ) { DigitalOutput(plugin, it) }
    }
}