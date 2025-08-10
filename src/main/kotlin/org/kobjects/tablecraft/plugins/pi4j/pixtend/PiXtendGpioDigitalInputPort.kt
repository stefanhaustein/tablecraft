package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*

class PiXtendGpioDigitalInputPort(
    val integration: PiXtendIntegration,
    val index: Int,
    listener: InputPortListener
) : PiXtendInputPortInstance(listener) {

    init {
        integration.driver?.setGpioMode(index, PiXtendDriver.GpioMode.DIGITAL_INPUT)
    }

    override val value: Any
        get() = integration.driver?.getGpioIn(index) ?: Unit

    override fun detach() {
        integration.inputPorts.remove(this)
    }

    companion object {
        fun spec(integration: PiXtendIntegration): InputPortSpec = InputPortSpec(
            "PiXtend",
            "pixt.gpio_din",
            Type.BOOL,
            "PiXtend GPIO port configured as digital input.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) { config, listener ->
            PiXtendGpioDigitalInputPort(integration, config["index"] as Int, listener).apply { integration.inputPorts.add(this) }
        }
    }
}