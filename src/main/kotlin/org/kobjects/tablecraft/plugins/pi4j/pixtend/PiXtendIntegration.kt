package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.pi4jdriver.plc.pixtend.PiXtendDriver
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.pi4j.Pi4jPlugin
import java.util.Timer
import java.util.TimerTask

class PiXtendIntegration(
    val pi4j: Pi4jPlugin,
    kind: String,
    name: String,
    tag: Long,
    var model: PiXtendDriver.Model

): IntegrationInstance(kind, name, tag) {
    var driver: PiXtendDriver? = null
    var error: Exception? = null
    val inputPorts = mutableSetOf<PiXtendInputPortInstance>()
    var threadNumber = 0

    init {
       attach()
    }

    fun attach() {
        try {
            driver = PiXtendDriver(pi4j.pi4J, model)
            error = null

            threadNumber++

            Thread{
                val myNumber = threadNumber
                val driver = driver!!
                while (myNumber == threadNumber) {
                    driver.syncState()
                    pi4j.model.applySynchronizedWithToken {
                        for (inputPort in inputPorts) {
                            inputPort.syncState(it)
                        }
                    }
                }
                driver.close()
            }.start()

        } catch (e: Exception) {
            e.printStackTrace()
            error = e
        }
    }

    companion object {

        val pixtendModel = Type.ENUM(PiXtendDriver.Model.entries)

        fun spec(pi4j: Pi4jPlugin) = IntegrationSpec(
            category = "PLC",
            name = "pixt",
            description = "PiXtend PLC Integration",
            parameters = listOf(ParameterSpec("model", pixtendModel, PiXtendDriver.Model.V2S)),
            modifiers = setOf(AbstractArtifactSpec.Modifier.SINGLETON),
        ) { kind, name, tag, config ->
            PiXtendIntegration(pi4j, kind, name, tag, config["model"] as PiXtendDriver.Model)
        }


    }

    override val operationSpecs: List<AbstractFactorySpec>
        get() = listOf(
            PiXtendAnalogInputPort.spec(this),
            PiXtendAnalogOutputPort.spec(this),
            PiXtendDigitalInputPort.spec(this),
            PiXtendDigitalOutputPort.spec(this),
            PiXtendGpioDigitalInputPort.spec(this),
            PiXtendGpioDigitalOutputPort.spec(this),
            PiXtendRelayPort.spec(this),
        )

    override val configuration: Map<String, Any?>
        get() = mapOf("model" to model.name)

    override fun detach() {
        threadNumber++

    }

    override fun reconfigure(configuration: Map<String, Any?>) {
        threadNumber++
        model = configuration["model"] as PiXtendDriver.Model
        attach()
    }


}