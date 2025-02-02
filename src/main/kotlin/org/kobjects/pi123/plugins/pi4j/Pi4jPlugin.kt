package org.kobjects.pi123.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.pluginapi.*

class Pi4jPlugin : Plugin {
    val pi4J = Pi4J.newAutoContext()

    val pins = mutableMapOf<Int, PinManager>()


    fun getPin(type: PinType, configuration: Map<String, Any>): PinManager {
        val address = (configuration["address"] as Number).toInt()
        val pin = pins[address]
        if (pin == null) {
            val newPin = PinManager(this, type, address, configuration)
            pins[address] = newPin
            return newPin
        }
        if (pin.type == type && pin.configuration == configuration) {
            return pin
        }
        throw IllegalStateException("Pin #$address is in use with a different configuration: $pin; requested: $type/$configuration")
    }

    override val functionSpecs = listOf(
        FunctionSpec(
            "din",
            "Digital Input",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) {  DigitalInputInstance(this, it) },
        FunctionSpec(
            "pwmin",
            "PWM Input",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { PwmInputInstance(this, it) },

        FunctionSpec(
            "dout",
            "Digital Output",
            listOf(
                ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("value", ParameterKind.RUNTIME, Type.INT),
                )
        ) { DigitalOutputInstance(this, it.configuration) },
    )

}