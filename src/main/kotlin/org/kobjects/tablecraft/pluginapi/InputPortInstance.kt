package org.kobjects.tablecraft.pluginapi

interface InputPortInstance {
    val value: Any

    fun detach()

}