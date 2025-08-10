package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*

class PiXtendAnalogInputPort(
    val integration: PiXtendIntegration,
    val index: Int,
    listener: InputPortListener
) : PiXtendInputPortInstance(listener) {

    override val value: Any
        get() = integration.driver?.getAnalogIn(index) ?: Unit

    override fun detach() {
        integration.inputPorts.remove(this)
    }

    companion object {
        fun spec(integration: PiXtendIntegration): InputPortSpec = InputPortSpec(
            "PiXtend",
            "pixt.ain",
            Type.REAL,
            "PiXtend analog input.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) { config, listener ->
            PiXtendAnalogInputPort(integration, config["index"] as Int, listener).apply { integration.inputPorts.add(this) }
        }
    }
}