package org.kobjects.pi123.pluginapi

interface OperationHost {
    val configuration: Map<String, Any>
    fun notifyValueChanged(newValue: Any)
}