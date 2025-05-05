package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.sensor.bmp280.Bmp280Driver
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*
import java.util.*

class Bmp280I2cIntegration(
    val plugin: Pi4jPlugin,
    configuration: Map<String, Any>
) : IntegrationInstance (
    configuration
) {
    var bmp280: Bmp280Driver?
    var error: Exception?

    val timer = Timer()
    val ports = mutableListOf<Bmp280Port>()

    init {
        try {
            val i2c: I2C = plugin.pi4J.create(
                I2C.newConfigBuilder(plugin.pi4J)
                    .bus(configuration["bus"] as Int? ?: 1)
                    .device(configuration["address"] as Int? ?: 0x76)
                    .provider("linuxfs-i2c")
                    .build()
            )

            bmp280 = Bmp280Driver.create(i2c)


            error = null
        } catch (e: Exception) {
            error = e
            bmp280 = null
        }
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (ports.isNotEmpty()) {
                    Model.applySynchronizedWithToken {
                        for (port in ports) {
                            port.update(it)
                        }
                    }
                }
            }
        }, 0, 100)
    }

    override val operationSpecs: List<AbstractArtifactSpec> = listOf(
        Bmp280Port.spec(this)
    )

    override val type: String
        get() = FACTORY_NAME

    override fun detach() {
        timer.cancel()
    }


    class Bmp280Port(
        val integration: Bmp280I2cIntegration,
        val kind: MeasurementType
    ) : InputPortInstance {
        var host: ValueChangeListener? = null
        var value: Double = Double.NaN

        fun update(token: ModificationToken) {
            val newValue = when(kind) {
                MeasurementType.C -> integration.bmp280?.temperatureC()
                MeasurementType.MBAR -> integration.bmp280?.pressureMb()
            } ?: Double.NaN
            if (value != newValue) {
                value = newValue
                host?.notifyValueChanged(token)
            }
        }

        override fun getValue(): Any {
            return value
        }

        override fun attach(host: ValueChangeListener) {
            this.host = host
            integration.ports.add(this)
        }

        override fun detach() {
            integration.ports.remove(this)
        }

        companion object {
            fun spec(integration: Bmp280I2cIntegration) = InputPortSpec(
                Type.REAL,
                integration.name + ".value",
                "Returns a measurement value of the BMP 280 sensor",
                parameters = listOf(ParameterSpec("kind", Type.ENUM(MeasurementType.entries), setOf(ParameterSpec.Modifier.CONSTANT)))
            ) {
                Bmp280Port(integration, it["kind"] as MeasurementType)
            }
        }
    }

    enum class MeasurementType {
        C, MBAR;
    }


    companion object {
        val FACTORY_NAME = "BMP280"

        fun spec(plugin: Pi4jPlugin) = IntegrationSpec(
            Type.VOID,
            FACTORY_NAME,
            "BMP 280 sensor",
            listOf(
                ParameterSpec("bus", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("address", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                )
        ) {
            Bmp280I2cIntegration(plugin, it)
        }
    }

}