package org.kobjects.tablecraft.plugins.pi4j


import freenove.Freenove_LCD1602
import org.kobjects.tablecraft.pluginapi.*

class Lcd1602(
    val plugin: Pi4jPlugin,
    val width: Int,
)  : OutputPortInstance {

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

    override fun setValue(value: Any) {
        TODO("Not yet implemented")
    }

    override fun attach() {
        TODO("Not yet implemented")
    }


    override fun detach() {
    }



    companion object {
        fun spec(plugin: Pi4jPlugin) = OutputPortSpec(
            Type.RANGE,
            "Lcd",
            "An LCD text display",
            listOf(
                ParameterSpec("width", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)),
        )) {
            Lcd1602(plugin, it["width"] as Int? ?: 16) }
    }

}