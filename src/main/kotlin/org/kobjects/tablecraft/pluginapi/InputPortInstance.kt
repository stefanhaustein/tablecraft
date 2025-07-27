package org.kobjects.tablecraft.pluginapi

interface InputPortInstance {
    fun getValue(): Any

    fun attach(host: ValueReceiver)

    fun detach()

    val type: Type
}