package org.kobjects.pi123.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.pluginapi.*

class Pi4jPlugin : Plugin {
    val pi4J = Pi4J.newAutoContext()

    val pins = mutableMapOf<Int, PinManager>()
    val digitalInputs = mutableMapOf<Int, DigitalInput>()
    val digitalOutputs = mutableMapOf<Int, DigitalOutput>()

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
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { configuration, callback -> DigitalInputInstance(this, configuration, callback) },
        FunctionSpec(
            "pwmin",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { configuration, callback -> PwmInputInstance(this, configuration, callback) },

        FunctionSpec(
            "dout",
            listOf(
                ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("value", ParameterKind.RUNTIME, Type.INT),
                )
        ) { configuration, _ -> DigitalOutputInstance(this, configuration) },
    )

}