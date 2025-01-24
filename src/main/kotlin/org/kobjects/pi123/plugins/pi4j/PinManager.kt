package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import com.pi4j.common.Identity

class PinManager(
    val plugin: Pi4jPlugin,
    val type: PinType,
    val address: Int,
    val configuration: Map<String, Any>
) : DigitalStateChangeListener {
    var pi4jPin: Identity? = null
    val listeners = mutableListOf<(Any)->Unit>()

    init {
        attach()
    }


    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        // println(event)
        for (listener in listeners) {
            try {
                listener(event!!.state().isHigh())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun attach() {
        pi4jPin = when (type) {
            PinType.DIGITAL_INPUT -> {
                val pin = plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
                pin.addListener(this)
                pin
            }
            PinType.DIGITAL_OUTPUT ->
                plugin.pi4J.create(DigitalOutputConfig.newBuilder(plugin.pi4J).address(address).build())
        }
    }

    fun getState(): Any {
        val pin = pi4jPin
        return when (pin) {
            is DigitalInput -> pin.isHigh()
            else -> Unit
        }
    }

    fun setState(state: Boolean) {
        val pin =pi4jPin
        when (pin) {
            is DigitalOutput -> pin.setState(state)
        }
    }


    override fun toString() = "$type:$address $configuration"
}