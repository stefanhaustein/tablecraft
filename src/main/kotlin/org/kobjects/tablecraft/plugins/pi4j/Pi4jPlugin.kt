package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalInputConfig
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalOutputConfig
import org.kobjects.tablecraft.pluginapi.*

class Pi4jPlugin(val model: ModelInterface) : Plugin {
    var pi4J = Pi4J.newAutoContext()

    val releasedPorts = mutableMapOf<Int, Any> ()

    fun createDigitalInput(config: DigitalInputConfig): DigitalInput {
        val address = config.address
        val existing = releasedPorts[address]
        return when (existing) {
            null -> pi4J.create(config)
            is DigitalInput -> releasedPorts.remove(address) as DigitalInput
            else -> throw IllegalStateException("Can't overwrite existing port $existing at address $address")
        }
    }

    fun createDigitalOutput(config: DigitalOutputConfig): DigitalOutput {
        val address = config.address
        val existing = releasedPorts[address]
        return when (existing) {
            null -> pi4J.create(config)
            is DigitalInput -> releasedPorts.remove(address) as DigitalOutput
            else -> throw IllegalStateException("Can't overwrite existing port $existing at address $address")
        }
    }

    fun releasePort(address: Int, port: Any?) {
        if (port != null) {
            releasedPorts[address] = port
        }
    }


    override val operationSpecs = listOf<AbstractArtifactSpec>(
        DigitalInputPort.spec(this),
        PwmInput.spec(this),
        DigitalOutputPort.spec(this),
        Lcd.spec(this),
        Bmp280Port.spec(this),
    )

}