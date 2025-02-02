package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.pluginapi.FunctionHost
import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val host: FunctionHost,
) : FunctionInstance {

    var pin: PinManager? = null

    override fun attach() {
       pin = plugin.getPin(PinType.DIGITAL_INPUT, host.configuration)
        println("Attached: $pin")

        pin!!.listeners.add (host::notifyValueChanged)

    }

    override fun apply(params: Map<String, Any>): Any =
        pin?.getState() ?: Unit

    override fun detach() {
        pin?.listeners?.remove(host::notifyValueChanged)
    }




}