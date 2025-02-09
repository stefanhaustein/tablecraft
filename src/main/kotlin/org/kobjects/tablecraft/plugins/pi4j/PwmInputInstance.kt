package org.kobjects.tablecraft.plugins.pi4j

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

class PwmInputInstance(
    val plugin: Pi4jPlugin,
    val host: OperationHost,
) : OperationInstance {

    var pin: PinManager? = null
    var t0: Long = 0
    var value: Double = 0.0

    fun callback(newValue: Any) {
        when (newValue) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    value = newValue
                    host.notifyValueChanged(value)
                }
            }
        }
    }

    override fun attach() {
       pin = plugin.getPin(PinType.DIGITAL_INPUT, host.configuration)
        println("Attached: $pin")

        pin!!.listeners.add (::callback)
    }

    override fun apply(params: Map<String, Any>): Any =
        value

    override fun detach() {

        pin?.listeners?.remove(::callback)

    }




}