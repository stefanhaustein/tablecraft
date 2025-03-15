package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote

data class OperationSpec(
    val kind: OperationKind,
    val returnType: Type,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> Operation,
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"kind":"$kind","returnType":"$returnType","description":${description.quote()},"params":[""")
        var first = true
        for (param in parameters.filter { it.kind != ParameterKind.RUNTIME }) {
            if (first) {
                first = false
            } else {
                sb.append(",")
            }
            param.toJson(sb)
        }
        sb.append("]}")
    }

    companion object {
        fun createTombstone(name: String, tag: Long) = OperationSpec(
            OperationKind.TOMBSTONE, Type.NUMBER, name, "Deleted Operation '$name'.", emptyList(), tag
        ) {
            throw UnsupportedOperationException("Tombstone for '$name' can't be instantiated.")
        }
    }
}

