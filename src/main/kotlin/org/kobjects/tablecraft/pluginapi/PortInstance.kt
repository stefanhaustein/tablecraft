package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

interface PortInstance: ToJson {
    val type: String
    val name: String
    val tag: Long
    val operationSpecs: List<OperationSpec>
    val configuration: Map<String, Any>

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "kind":"PORT", "type":${type.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append("}")
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