package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.tablecraft.pluginapi.*

class DigitalInput(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
) : InputPortInstance, Pi4JPortHolder, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var error: Exception? = null
    var value = false
    var host: ValueChangeListener? = null

    override fun attach(host: ValueChangeListener) {
        this.host = host
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (configuration["address"] as Number).toInt()
        try {
            digitalInput = plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
            digitalInput?.addListener(this)
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            digitalInput = null
        }
    }

    override fun getValue(): Any {
        if (error != null) {
            throw error!!
        }
        return value
    }

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        plugin.model.applySynchronizedWithToken {
            host?.notifyValueChanged(it)
        }
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
        digitalInput?.removeListener(this)
    }

    companion object {
        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            Type.BOOL,
            "din",
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT))),
        ) { DigitalInput(plugin, it) }
    }
}