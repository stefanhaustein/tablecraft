package org.kobjects.tablecraft.pluginapi

interface OperationInstance {

    fun attach()

    fun apply(params: Map<String, Any>): Any

    fun detach()
}