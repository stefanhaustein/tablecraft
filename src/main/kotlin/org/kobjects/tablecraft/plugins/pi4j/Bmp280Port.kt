package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.i2c.I2C
import org.kobjects.pi4jdriver.sensor.environment.bmx280.Bmx280Driver
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*
import java.util.*

class Bmp280Port(
    val plugin: Pi4jPlugin,
    val bus: Int,
    val address: Int

) : InputPortInstance {

    var bmp280: Bmx280Driver? = null
    var error: Exception? = null
    var host: ValueChangeListener? = null
    val timer = Timer()
    var value = emptyMap<String, Double?>()

    override fun attach(host: ValueChangeListener) {
        this.host = host
        try {
            val i2c: I2C = plugin.pi4J.create(
                I2C.newConfigBuilder(plugin.pi4J)
                    .bus(bus)
                    .device(address)
                    .provider("linuxfs-i2c")
                    .build()
            )

            bmp280 = Bmx280Driver.create(i2c)

            error = null

            timer.schedule(object : TimerTask() {
                override fun run() {
                    poll()
                }
            }, 0, 10000)

            System.err.println("**** BMx280 driver successfully attached: $bmp280")

        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            bmp280 = null
        }
    }

    fun poll() {
        val measurement = bmp280?.readMeasurements()
        val newValue = mapOf(
            "temperature" to measurement?.getTemperature(),
            "pressure" to measurement?.getPressure(),
            "humidity" to measurement?.getHumidity(),
        )
        Model.applySynchronizedWithToken {
            value = newValue
            host?.notifyValueChanged(it)
        }

    }

    override fun detach() {
        timer.cancel()
    }

    override val type: Type
        get() = TYPE

    override fun getValue(): Any {
        return value
    }


    companion object {
        val TYPE = Type.Struct(listOf(
            Type.Field("temperature", Type.REAL),
            Type.Field("pressure", Type.REAL),
            Type.Field("humidity", Type.REAL),
        ))

        fun spec(plugin: Pi4jPlugin) = InputPortSpec(
            category = "Driver",
            "Bmp280",
            "BMP 280 sensor port.",
            listOf(
                ParameterSpec(
                    "bus",
                    Type.INT,
                    null,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                ),
                ParameterSpec(
                    "address",
                    Type.INT,
                    null,
                    setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)
                )),
            createFn = { Bmp280Port(plugin, it["bus"] as? Int ?: 1, it ["address"] as? Int ?:  0x77) },
        )
    }
}