package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.io.gpio.digital.DigitalInput
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.StatefulOperation

class DigitalInput(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
) : StatefulOperation, Pi4JPort, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var error: Exception? = null
    var value = false
    var host: OperationHost? = null

    override fun attach(host: OperationHost) {
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

    override fun apply(params: Map<String, Any>): Any {
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

}