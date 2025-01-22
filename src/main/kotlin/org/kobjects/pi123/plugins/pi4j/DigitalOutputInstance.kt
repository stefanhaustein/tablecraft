package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import io.ktor.util.*
import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalOutputInstance(
    val plugin: Pi4jPlugin,
    val address: Int
) : FunctionInstance {

    val digitalOutput: DigitalOutput = plugin.digitalOutputs.getOrPut(address) {
        plugin.pi4J.create(DigitalOutputConfig.newBuilder(plugin.pi4J).address(address).build())
    }

    override fun apply(params: Map<String, Any>) {
        val value = when(val raw = params["value"]) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw")
        }
        digitalOutput.setState(value)
    }

    override fun attach() {
    }

    override fun detach() {
    }


    companion object {

        fun create(
            plugin: Pi4jPlugin,
            configuration: Map<String, Any>
        ): DigitalOutputInstance {
            require(configuration.size == 1)
            val address = (configuration["address"] as Number).toInt()

            return DigitalOutputInstance(plugin, address)
        }

    }


}