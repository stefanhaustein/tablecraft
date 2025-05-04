package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import org.kobjects.tablecraft.pluginapi.*

class Pi4jPlugin(val model: ModelInterface) : Plugin {
    var pi4J = Pi4J.newAutoContext()

    val ports = mutableListOf<Pi4JPortHolder>()

    fun addPort(port: Pi4JPortHolder) {
        ports.add(port)
    }

    fun removePort(remove: Pi4JPortHolder) {
        ports.remove(remove)
        for (port in ports) {
            port.detachPort()
        }
        pi4J.shutdown()
        pi4J = Pi4J.newAutoContext()
        for (port in ports) {
            port.attachPort()
        }
    }

    override val operationSpecs = listOf<AbstractArtifactSpec>(
        DigitalInput.spec(this),
        PwmInput.spec(this),
        DigitalOutput.spec(this),
        Lcd1602.spec(this),
    )

}