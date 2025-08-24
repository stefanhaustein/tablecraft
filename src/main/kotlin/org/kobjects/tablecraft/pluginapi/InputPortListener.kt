package org.kobjects.tablecraft.pluginapi

interface InputPortListener {
    fun portValueChanged(token: ModificationToken, newValue: Any?)
}