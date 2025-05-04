package org.kobjects.tablecraft.pluginapi

interface OutputPortInstance {
    fun setValue(value: Any)

    fun attach()

    fun detach()
}