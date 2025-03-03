package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import org.kobjects.tablecraft.pluginapi.*

class Pi4jPlugin : Plugin {
    var pi4J = Pi4J.newAutoContext()

    val ports = mutableListOf<Pi4JPort>()

    fun addPort(port: Pi4JPort) {
        ports.add(port)
    }

    fun removePort(remove: Pi4JPort) {
        ports.remove(remove)
        for (port in ports) {
            port.detachPort()
        }
        pi4J.shutdown()
        pi4J = Pi4J.newAutoContext()
    }

    override val operationSpecs = listOf<OperationSpec>(
        OperationSpec(
            OperationKind.INPUT_PORT,
            Type.BOOLEAN,
            "din",
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT)),
        ) { DigitalInputInstance(this, it) },
        OperationSpec(
            OperationKind.INPUT_PORT,
            Type.NUMBER,
            "pwmin",
            "Configures the given pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT)),
        ) { PwmInputInstance(this, it) },
        OperationSpec(
            OperationKind.OUTPUT_PORT,
            Type.BOOLEAN,
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("value", ParameterKind.RUNTIME, Type.INT)),
        ) { DigitalOutputInstance(this, it.configuration) },
        OperationSpec(
            OperationKind.OUTPUT_PORT,
            Type.TEXT,
            "lcddisplay",
            "Configures the size of a lcd display",
            listOf(
                ParameterSpec("width", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("height", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("x", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("y", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("text", ParameterKind.RUNTIME, Type.TEXT))
            ) { LcdInstance(this, it.configuration) },
    )

}