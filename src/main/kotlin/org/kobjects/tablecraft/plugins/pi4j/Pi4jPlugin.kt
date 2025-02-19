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

    override val portSpecs = listOf<PortSpec>(
        SimplePortSpec(
            "din",
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT)),
            Type.BOOLEAN,
            emptyList()
        ) { DigitalInputInstance(this, it) },
        SimplePortSpec(
            "pwmin",
            "Configures the given pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT)),
            Type.NUMBER,
            emptyList()
        ) { PwmInputInstance(this, it) },
        SimplePortSpec(
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT)),
            Type.BOOLEAN,
            listOf(ParameterSpec("value", ParameterKind.RUNTIME, Type.INT)),
        ) { DigitalOutputInstance(this, it.configuration) },
        SimplePortSpec(
            "lcddisplay",
            "Configures the size of a lcd display",
            listOf(
                ParameterSpec("width", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("height", ParameterKind.CONFIGURATION, Type.INT)
            ),
            Type.TEXT,
            listOf(
                ParameterSpec("x", ParameterKind.RUNTIME, Type.INT),
                ParameterSpec("y", ParameterKind.RUNTIME, Type.INT),
                ParameterSpec("text", ParameterKind.RUNTIME, Type.TEXT))
            ) { LcdInstance(this, it.configuration) },
    )

    override val operationSpecs = emptyList<OperationSpec>()

}