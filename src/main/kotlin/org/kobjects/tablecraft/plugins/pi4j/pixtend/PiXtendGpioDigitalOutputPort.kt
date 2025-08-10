package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*

class PiXtendGpioDigitalOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {

    init {
        integration.driver?.setGpioMode(index, PiXtendDriver.GpioMode.DIGITAL_OUTPUT)
    }

    override fun setValue(value: Any?) {
        integration.driver?.setGpioOut(index, value as Boolean)
    }

    override fun detach() {

    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            "PiXtend",
            "pixt.gpio_dout",
       //     Type.REAL,
            "PiXtend GPIO configured as digital output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendGpioDigitalOutputPort(integration, it["index"] as Int)
        }
    }
}