package org.kobjects.tablecraft.pluginapi

interface FunctionInstance {
    fun apply(params: Map<String, Any>): Any
}