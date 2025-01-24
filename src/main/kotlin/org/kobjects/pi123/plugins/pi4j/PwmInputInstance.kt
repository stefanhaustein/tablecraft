package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.pluginapi.FunctionInstance

class PwmInputInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>,
    val callback: ((Any) -> Unit)?
) : FunctionInstance {

    var pin: PinManager? = null
    var t0: Long = 0
    var value: Double = 0.0

    override fun attach() {
       pin = plugin.getPin(PinType.DIGITAL_INPUT, configuration)
        println("Attached: $pin")
        if (callback != null) {
            println("Callback added: $callback")
            pin!!.listeners.add {
                when (it) {
                    true -> {
                        t0 = System.currentTimeMillis()
                    }
                    false -> {
                        val newValue = (System.currentTimeMillis() - t0) / 1000.0
                        if (newValue != value && t0 != 0L) {
                            value = newValue
                            callback?.invoke(value)
                        }
                    }
                    else -> it
                }

            }
        }
    }

    override fun apply(params: Map<String, Any>): Any =
        value

    override fun detach() {
        if (callback != null) {
            pin?.listeners?.remove(callback)
        }
    }




}