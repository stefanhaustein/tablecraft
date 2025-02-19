package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.quote

interface PortInstance {
    val type: String
    val name: String
    val tag: Long
    val operationSpecs: List<OperationSpec>
    val configuration: Map<String, Any>


    fun toJson(): String {
        val configJson = configuration.entries.joinToString {  "${it.key.quote()}: ${it.value.toString().quote()}" }

        return """{"name":${name.quote()}, "kind":"PORT", "type":${type.quote()}, "configuration": {$configJson} } """
    }

}