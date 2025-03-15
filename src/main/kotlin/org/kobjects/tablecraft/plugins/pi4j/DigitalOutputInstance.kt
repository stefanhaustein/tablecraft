package org.kobjects.tablecraft.plugins.pi4j


import com.pi4j.io.gpio.digital.*
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

class DigitalOutputInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
)  : OperationInstance, Pi4JPort {

    var digitalOutput: DigitalOutput? = null
    var error: Exception? = null

    override fun attach(host: OperationHost) {
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

    override fun apply(params: Map<String, Any>): Any {
        if (error != null) {
            throw error!!
        }
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