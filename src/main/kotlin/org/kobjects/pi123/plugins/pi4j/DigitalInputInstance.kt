package org.kobjects.pi123.plugins.pi4j

import org.kobjects.pi123.pluginapi.OperationHost
import org.kobjects.pi123.pluginapi.OperationInstance

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val host: OperationHost,
) : OperationInstance {

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