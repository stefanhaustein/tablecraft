package org.kobjects.tablecraft.plugins.pi4j


import freenove.Freenove_LCD1602
import org.kobjects.tablecraft.pluginapi.*

class Lcd1602(
    val plugin: Pi4jPlugin,
    configuration: Map<String, Any>
)  : IntegrationInstance(configuration) {

    override val type: String
        get() = "Lcd1602"

    var display: Freenove_LCD1602?
    var error: Exception?

    init {
        try {
            display = Freenove_LCD1602()
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            display = null
        }
    }



    override fun detach() {
    }

    override val operationSpecs: List<AbstractFactorySpec> = listOf(
        OutputPortSpec(
            Type.STRING,
            name + ".section",
            ".",
            listOf(
                ParameterSpec("x", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("y", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("width", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)),
            ),
            emptySet(),
            tag
        ) {
            Section(it["x"] as Int, it["y"] as Int)
        })


    inner class Section(val x: Int, val y: Int) : OutputPortInstance {

        override fun attach() {}

        override fun setValue(value: Any) {
            val text = value.toString()
            display?.position(x, y)
            display?.puts(text)
        }

        override fun detach() {}
    }

    companion object {
        fun spec(plugin: Pi4jPlugin) = IntegrationSpec(
            "Lcd1602",
            "Configures the size of a lcd display",
            listOf(
                ParameterSpec("width", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("height",  Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)))
        ) {
            Lcd1602(plugin, it) }
    }

}