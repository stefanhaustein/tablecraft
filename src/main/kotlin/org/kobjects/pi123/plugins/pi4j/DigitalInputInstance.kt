package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val address: Int,
    val callback: ((Any) -> Unit)?
) : FunctionInstance, DigitalStateChangeListener {

    val digitalInput: DigitalInput = plugin.digitalInputs.getOrPut(address) {
        plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
    }

    override fun apply(params: Map<String, Any>) =
        digitalInput.isOn


    override fun detach() {
        digitalInput.removeListener(this)
    }


    companion object {

        fun create(
            plugin: Pi4jPlugin,
            configuration: Map<String, Any>,
            callback: ((Any) -> Unit)?
        ): DigitalInputInstance {
            require(configuration.size == 1)
            val address = (configuration["address"] as Number).toInt()

            return DigitalInputInstance(plugin, address, callback)
        }

    }

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        callback?.invoke(digitalInput.isOn)
    }

}