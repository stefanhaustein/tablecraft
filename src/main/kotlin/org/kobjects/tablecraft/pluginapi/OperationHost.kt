package org.kobjects.tablecraft.pluginapi

interface OperationHost {
    val configuration: Map<String, Any>
    fun notifyValueChanged(newValue: Any)
}