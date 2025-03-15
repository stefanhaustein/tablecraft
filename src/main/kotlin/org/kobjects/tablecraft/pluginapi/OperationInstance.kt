package org.kobjects.tablecraft.pluginapi

interface OperationInstance {

    fun attach(host: OperationHost)

    fun apply(params: Map<String, Any>): Any

    fun detach()
}