package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.tablecraft.pluginapi.*

class PiXtendDigitalInputPort(
    val integration: PiXtendIntegration,
    val index: Int,
    listener: InputPortListener
) : PiXtendInputPortInstance(listener) {

    override val value: Any
        get() = integration.driver?.getDigitalIn(index) ?: Unit

    override fun detach() {
        integration.inputPorts.remove(this)
    }

    companion object {
        fun spec(integration: PiXtendIntegration): InputPortSpec = InputPortSpec(
            "PiXtend",
            "pixt.din",
            Type.BOOL,
            "PiXtend digital input.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) { config, listener ->
            PiXtendDigitalInputPort(integration, config["index"] as Int, listener).apply { integration.inputPorts.add(this) }
        }
    }
}