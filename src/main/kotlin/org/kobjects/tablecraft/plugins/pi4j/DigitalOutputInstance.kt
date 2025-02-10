package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.gpio.digital.*
import org.kobjects.tablecraft.pluginapi.OperationInstance

class DigitalOutputInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
)  : OperationInstance, Pi4JPort {

    var digitalOutput: DigitalOutput? = null

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (configuration["address"] as Number).toInt()
        digitalOutput = plugin.pi4J.create(DigitalOutputConfig.newBuilder(plugin.pi4J).address(address).build())
    }

    override fun apply(params: Map<String, Any>): Any {
        val value = when(val raw = params["value"]) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw; all params: $params")
        }
        digitalOutput!!.setState(value)
        return value
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
    }
}