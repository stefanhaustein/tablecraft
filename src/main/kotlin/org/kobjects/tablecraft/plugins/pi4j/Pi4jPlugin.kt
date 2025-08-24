package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.context.Context
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.pi4j.devices.Bmp280Port
import org.kobjects.tablecraft.plugins.pi4j.devices.Scd4xPort
import org.kobjects.tablecraft.plugins.pi4j.pixtend.PiXtendIntegration

class Pi4jPlugin(val model: ModelInterface) : Plugin {
    var pi4j: Context? = null
    var error: Exception? = null

    init {
        reInit()
    }

    override fun notifySimulationModeChanged(token: ModificationToken) {
        reInit()
    }

    fun reInit() {
        if (Model.simulationMode == (pi4j == null)) {
            return
        }
        try {
            if (model.simulationMode) {
                pi4j?.shutdown()
            } else {
                pi4j = Pi4J.newAutoContext()
            }
        } catch (e: Exception) {
            pi4j = null
            error = e
        }
    }

    override val operationSpecs = listOf<AbstractArtifactSpec>(
        DigitalInputPort.spec(this),
        PwmInput.spec(this),
        DigitalOutputPort.spec(this),
        TextLcd.spec(this),
        Bmp280Port.spec(this),
        Scd4xPort.spec(this),
        PiXtendIntegration.spec(this),
    )

}