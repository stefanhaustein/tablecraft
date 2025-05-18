package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.sensor.environment.bmx280.Bmx280Driver
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*
import java.util.*

class Bmp280Integration(
    val plugin: Pi4jPlugin,
    kind: String,
    name: String,
    tag: Long,
    configuration: Map<String, Any>
) : IntegrationInstance (
    kind, name, tag
) {
    var bmp280: Bmx280Driver? = null
    var error: Exception? = null

    val timer = Timer()
    val ports = mutableListOf<Bmp280Port>()
    override var configuration: Map<String, Any> = emptyMap()

    init {
        reconfigure(configuration)

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

    override fun reconfigure(configuration: Map<String, Any>) {
        this.configuration = configuration
        try {
            val i2c: I2C = plugin.pi4J.create(
                I2C.newConfigBuilder(plugin.pi4J)
                    .bus(configuration["bus"] as Int? ?: 1)
                    .device(configuration["address"] as Int? ?: 0x76)
                    .provider("linuxfs-i2c")
                    .build()
            )

            bmp280 = Bmx280Driver.create(i2c)

            error = null
        } catch (e: Exception) {
            error = e
            bmp280 = null
        }

    }

    override val operationSpecs: List<AbstractFactorySpec> = listOf(
        Bmp280Port.spec(this)
    )

    override fun detach() {
        timer.cancel()
    }


    class Bmp280Port(
        val integration: Bmp280Integration,
        val kind: MeasurementType
    ) : InputPortInstance {
        var host: ValueChangeListener? = null
        var value: Double = Double.NaN

        fun update(token: ModificationToken) {
            val newValue = when(kind) {
                MeasurementType.TEMP_C -> integration.bmp280?.getTemperatureC()
                MeasurementType.TEMP_F -> integration.bmp280?.getTemperatureF()
                MeasurementType.PRESS_PA -> integration.bmp280?.getPressurePa()
                MeasurementType.PRESS_MBAR -> integration.bmp280?.getPressureMb()
                MeasurementType.PRESS_IN_HG -> integration.bmp280?.getPressureInHg()
                MeasurementType.HUMIDITY -> integration.bmp280?.getHumidity()
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
            fun spec(integration: Bmp280Integration) = InputPortSpec(
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
        TEMP_C, TEMP_F,
        PRESS_MBAR, PRESS_PA, PRESS_IN_HG,
        HUMIDITY;
    }


    companion object {
        val FACTORY_NAME = "BMP280"

        fun spec(plugin: Pi4jPlugin) = IntegrationSpec(
            FACTORY_NAME,
            "BMP 280 sensor",
            listOf(
                ParameterSpec("bus", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("address", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                )
        ) { FACTORY_NAME, name, tag, configuration ->
            Bmp280Integration(plugin, FACTORY_NAME, name, tag, configuration)
        }
    }

}