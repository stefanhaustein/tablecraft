package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

data class OperationSpec(
    val kind: OperationKind,
    val returnType: Type,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val modifiers: Set<Modifier> = emptySet(),
    val tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> Any,
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"kind":"$kind","returnType":"$returnType","description":${description.quote()},"params":[""")
        var first = true
        for (param in parameters) {
            if (first) {
                first = false
            } else {
                sb.append(",")
            }
            param.toJson(sb)
        }
        sb.append("]")
        if (modifiers.isNotEmpty()) {
            sb.append(""","modifiers":[""")
            sb.append(modifiers.joinToString(",") { it.name.quote() })
            sb.append("]")
        }
        sb.append("}")
    }

    companion object {
        fun createTombstone(name: String, tag: Long) = OperationSpec(
            OperationKind.TOMBSTONE, Type.NUMBER, name, "Deleted Operation '$name'.", emptyList(), emptySet(),  tag
        ) {
            throw UnsupportedOperationException("Tombstone for '$name' can't be instantiated.")
        }
    }

    enum class Modifier {

    }
}

