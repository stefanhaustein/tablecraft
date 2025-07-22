package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.tablecraft.pluginapi.*

class PwmInput(
    val plugin: Pi4jPlugin,
    val address: Int
) : InputPortInstance, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var t0: Long = 0
    var value: Double = 0.0
    var error: Exception? = IllegalStateException("Detached")
    var host: ValueChangeListener? = null

    override fun attach(host: ValueChangeListener) {
        this.host = host
        try {
            digitalInput = plugin.createDigitalInput(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            digitalInput = null
        }
        digitalInput?.addListener(this)
    }

    override fun getValue(): Any = value

    override val type: Type
        get() = Type.REAL

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        when (event!!.state().isHigh()) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    value = newValue
                    plugin.model.applySynchronizedWithToken { token ->
                        host!!.notifyValueChanged(token)
                    }
                }
            }
        }
    }

    override fun detach() {
        digitalInput?.removeListener(this)
        plugin.releasePort(address, digitalInput)
        digitalInput = null
        error = IllegalStateException("Detached")
    }

    companion object {
        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            category = "GPIO",
            "pwmin",
            "Configures the given pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT))),
            createFn = {
                PwmInput(plugin, it["address"] as Int)
            },
        )
    }

}