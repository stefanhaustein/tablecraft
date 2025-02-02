package org.kobjects.pi123.pluginapi

interface FunctionHost {
    val configuration: Map<String, Any>
    fun notifyValueChanged(newValue: Any)
}