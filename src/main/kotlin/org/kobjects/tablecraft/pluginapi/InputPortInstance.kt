package org.kobjects.tablecraft.pluginapi

interface InputPortInstance {
    fun getValue(): Any

    fun attach(host: ValueChangeListener)

    fun detach()

    val type: Type
}