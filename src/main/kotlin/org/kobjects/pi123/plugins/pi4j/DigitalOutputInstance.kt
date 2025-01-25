package org.kobjects.pi123.plugins.pi4j


import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalOutputInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
) : FunctionInstance {

    var pin: PinManager? = null

    override fun apply(params: Map<String, Any>): Any {
        val value = when(val raw = params["value"]) {
            is Boolean -> raw
            is Number -> raw.toDouble() != 0.0
            else -> throw IllegalArgumentException("Unsupported value type for digital input: $raw; all params: $params")
        }
        pin!!.setState(value)
        return value
    }

    override fun attach() {
        pin = plugin.getPin(PinType.DIGITAL_OUTPUT, configuration)
        println("Attached pin: $pin")
    }

    override fun detach() {
    }




}