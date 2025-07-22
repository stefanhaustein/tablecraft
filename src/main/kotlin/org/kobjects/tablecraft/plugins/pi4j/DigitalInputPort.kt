package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.tablecraft.pluginapi.*

class DigitalInputPort(
    val plugin: Pi4jPlugin,
    val address: Int
) : InputPortInstance, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var error: Exception? = IllegalStateException("Detached")
    var value = false
    var host: ValueChangeListener? = null

    override fun attach(host: ValueChangeListener) {
        this.host = host
        try {
            digitalInput = plugin.createDigitalInput(
                DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
            digitalInput?.addListener(this)
            error = null
            value = digitalInput?.isHigh ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            digitalInput = null
        }
    }

    override fun getValue(): Any {
        if (error != null) {
            throw RuntimeException(error!!)
        }
        return value
    }

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        plugin.model.applySynchronizedWithToken {
            value = event?.state()?.isHigh ?: false
            host?.notifyValueChanged(it)
        }
    }

    override fun detach() {
        digitalInput?.removeListener(this)
        plugin.releasePort(address, digitalInput)
        digitalInput = null
    }

    override val type: Type
        get() = Type.BOOL

    companion object {
        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            category = "GPIO",
            Type.BOOL,
            "din",
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT))),
            createFn = { DigitalInputPort(plugin, it["address"] as Int) },
        )
    }
}