package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val address: Int,
    val callback: ((Any) -> Unit)?
) : FunctionInstance, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null

    override fun attach() {
       digitalInput = plugin.digitalInputs.getOrPut(address) {
            plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
        }
        digitalInput?.addListener(this)
    }

    override fun apply(params: Map<String, Any>) =
        digitalInput?.isOn ?: false


    override fun detach() {
        digitalInput?.removeListener(this)
    }

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        callback?.invoke(digitalInput?.isOn ?: false)
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

}