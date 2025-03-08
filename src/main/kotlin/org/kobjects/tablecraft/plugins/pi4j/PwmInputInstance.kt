package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance
import org.kobjects.tablecraft.pluginapi.OperationKind

class PwmInputInstance(
    val plugin: Pi4jPlugin,
    val host: OperationHost,
) : OperationInstance, Pi4JPort, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var t0: Long = 0
    var value: Double = 0.0
    var error: Exception? = null

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (host.configuration["address"] as Number).toInt()
        try {
            digitalInput = plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
            error = null
        } catch (e: Exception) {
            error = e
            digitalInput = null
        }
        digitalInput?.addListener(this)
    }

    override fun apply(params: Map<String, Any>): Any = value

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        when (event!!.state().isHigh()) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    value = newValue
                    ModificationToken.applySynchronizedWithToken { token ->
                        host.notifyValueChanged(value, token)
                    }
                }
            }
        }
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
        digitalInput?.removeListener(this)
    }


}