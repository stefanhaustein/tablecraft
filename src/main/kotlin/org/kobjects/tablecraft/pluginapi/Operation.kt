package org.kobjects.tablecraft.pluginapi

interface Operation {
    fun apply(params: Map<String, Any>): Any
}