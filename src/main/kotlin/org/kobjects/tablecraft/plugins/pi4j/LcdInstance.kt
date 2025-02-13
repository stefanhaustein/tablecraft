package org.kobjects.tablecraft.plugins.pi4j


import freenove.I2CLCD1602
import org.kobjects.tablecraft.pluginapi.OperationInstance

class LcdInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
)  : OperationInstance, Pi4JPort {

    var error: Exception? = null
    var display: I2CLCD1602? = null

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val width = (configuration["width"] as Number).toInt()
        val height = (configuration["height"] as Number).toInt()
        try {
            display = I2CLCD1602()
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            display = null
        }
    }

    override fun apply(params: Map<String, Any>): Any {
        val x = (params["x"] as Number).toInt()
        val y = (params["y"] as Number).toInt()
        val text = params["text"].toString()
        display?.printAt(x, y, text)
        return text
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
    }
}