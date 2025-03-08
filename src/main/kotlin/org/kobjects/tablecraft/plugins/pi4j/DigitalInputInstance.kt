package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance
import org.kobjects.tablecraft.pluginapi.OperationKind

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val host: OperationHost,
) : OperationInstance, Pi4JPort, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var error: Exception? = null
    var value = false

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (host.configuration["address"] as Number).toInt()
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

    override fun apply(params: Map<String, Any>): Any {
        if (error != null) {
            throw error!!
        }
        return value
    }

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        ModificationToken.applySynchronizedWithToken {
            host.notifyValueChanged(event!!.state().isHigh(), it)
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