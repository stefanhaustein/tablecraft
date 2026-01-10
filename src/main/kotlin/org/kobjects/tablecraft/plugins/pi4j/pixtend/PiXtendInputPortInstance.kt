package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.tablecraft.pluginapi.InputPortInstance
import org.kobjects.tablecraft.pluginapi.InputPortListener
import org.kobjects.tablecraft.pluginapi.ModelInterface
import org.kobjects.tablecraft.pluginapi.ModificationToken

abstract class PiXtendInputPortInstance(
    val integration: PiXtendIntegration,
    val listener: InputPortListener) : InputPortInstance {

    var lastValue: Any? = null

    fun syncState(token: ModificationToken) {
        val newValue = value
        if (newValue != lastValue) {
            listener.portValueChanged(token, newValue)
            lastValue = newValue
        }
    }

}