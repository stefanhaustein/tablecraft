package org.kobjects.pi123.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.pluginapi.*

class Pi4jPlugin : Plugin {
    val pi4J = Model.pi4J // Pi4J.newAutoContext()

    val digitalInputs = mutableMapOf<Int, DigitalInput>()
    val digitalOutputs = mutableMapOf<Int, DigitalOutput>()

    override val functionSpecs = listOf(
        FunctionSpec(
            "din",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { configuration, callback -> DigitalInputInstance.create(this, configuration, callback) },

        FunctionSpec(
    "dout",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { configuration, _ -> DigitalOutputInstance.create(this, configuration) },
    )

}