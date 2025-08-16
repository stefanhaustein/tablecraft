package org.kobjects.tablecraft.plugins.pi4j.pixtend

import org.kobjects.tablecraft.pluginapi.InputPortInstance
import org.kobjects.tablecraft.pluginapi.InputPortListener
import org.kobjects.tablecraft.pluginapi.ModificationToken

abstract class PiXtendInputPortInstance(val listener: InputPortListener) : InputPortInstance {

    var lastValue: Any? = null

    fun syncState(token: ModificationToken) {
        val newValue = value
        if (newValue != lastValue) {
            lastValue = newValue
            listener.updateValue(token, newValue)
        }
    }

}