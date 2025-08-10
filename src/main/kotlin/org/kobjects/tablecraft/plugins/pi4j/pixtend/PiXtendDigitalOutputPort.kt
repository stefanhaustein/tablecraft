package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*

class PiXtendDigitalOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {


    override fun setValue(value: Any?) {
        integration.driver?.setDigitalOut(index, value as Boolean)
    }

    override fun detach() {

    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            "PiXtend",
            "pixt.dout",
       //     Type.REAL,
            "PiXtend digital output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendDigitalOutputPort(integration, it["index"] as Int)
        }
    }
}