package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.quote

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

    class Tombstone(
        override val name: String,
        override val tag: Long
    ): PortInstance {
        override val type: String
            get() = "Tombstone"

        override val operationSpecs: List<OperationSpec>
            get() = emptyList()
        override val configuration: Map<String, Any>
            get() = emptyMap()

    }

}