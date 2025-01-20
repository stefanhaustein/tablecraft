package org.kobjects.pi123.pluginapi

interface FunctionInstance {

    fun apply(params: Map<String, Any>): Any

    fun detach()
}