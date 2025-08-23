package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalInputConfig
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalOutputConfig
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.pi4j.devices.Bmp280Port
import org.kobjects.tablecraft.plugins.pi4j.devices.Scd4xPort
import org.kobjects.tablecraft.plugins.pi4j.pixtend.PiXtendIntegration

class Pi4jPlugin(val model: ModelInterface) : Plugin {
    var pi4j = Pi4J.newAutoContext()

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