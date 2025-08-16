package org.kobjects.tablecraft.pluginapi

interface InputPortListener {
    fun updateValue(token: ModificationToken, newValue: Any?)
}