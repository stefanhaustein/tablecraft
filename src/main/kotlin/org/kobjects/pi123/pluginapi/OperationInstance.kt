package org.kobjects.pi123.pluginapi

interface OperationInstance {

    fun attach()

    fun apply(params: Map<String, Any>): Any

    fun detach()
}