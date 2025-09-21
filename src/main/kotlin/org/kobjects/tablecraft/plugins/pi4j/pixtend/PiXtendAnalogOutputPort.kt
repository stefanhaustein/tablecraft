package org.kobjects.tablecraft.plugins.pi4j.pixtend

import com.pi4j.drivers.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*

class PiXtendAnalogOutputPort(
    val integration: PiXtendIntegration,
    val index: Int,
) : OutputPortInstance {

    init {
        integration.driver?.setAnalogOutEnabled(index, true)
    }

    override fun setValue(value: Any?) {
        integration.driver?.setAnalogOut(index, value as Double)
    }

    override fun detach() {
        integration.driver?.setAnalogOutEnabled(index, false)
    }

    companion object {
        fun spec(integration: PiXtendIntegration) = OutputPortSpec(
            "PiXtend",
            "pixt.aout",
       //     Type.REAL,
            "PiXtend analog output.",
            listOf(ParameterSpec("index", Type.INT, 0)),
            emptySet(),
            integration.tag
        ) {
            PiXtendAnalogOutputPort(integration, it["index"] as Int)
        }
    }
}