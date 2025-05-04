package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.sensor.bmp280.Bmp280Driver
import org.kobjects.tablecraft.pluginapi.*

class Bmp280I2cIntegration(
    val plugin: Pi4jPlugin,
    configuration: Map<String, Any>
) : IntegrationInstance(
    configuration
) {
    val bmp280: Bmp280Driver

    init {
        val i2c: I2C = plugin.pi4J.create(
            I2C.newConfigBuilder(plugin.pi4J)
                .bus(configuration["bus"] as Int? ?: 1)
                .device(configuration["address"] as Int? ?: 0x76)
                .provider("linuxfs-i2c")
                .build()
        )

        bmp280 = Bmp280Driver.create(i2c)
    }

    override val operationSpecs: List<AbstractArtifactSpec> = listOf(
        Bmp280Port.spec(this)
    )

    override val type: String
        get() = "BMP280"

    override fun detach() {

    }


    class Bmp280Port(integration: Bmp280I2cIntegration, kind: MeasurementType) : InputPortInstance {
        var host: ValueChangeListener? = null

        override fun getValue(): Any {
            return 0.0
        }

        override fun attach(host: ValueChangeListener) {
            this.host = host
        }

        override fun detach() {

        }

        companion object {
            fun spec(integration: Bmp280I2cIntegration) = InputPortSpec(
                Type.REAL,
                integration.name + "_measurement",
                "Returns a measurement value of the BMP 280 sensor",
                parameters = listOf(ParameterSpec("kind", Type.ENUM(MeasurementType.entries)))
            ) {
                Bmp280Port(integration, it["kind"] as MeasurementType)
            }
        }
    }

    enum class MeasurementType {
        C, MBAR;
    }


    companion object {

        fun spec(plugin: Pi4jPlugin) = IntegrationSpec(
            Type.VOID,
            "BMP280",
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