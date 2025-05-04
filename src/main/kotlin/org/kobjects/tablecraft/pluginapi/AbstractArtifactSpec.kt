package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote

abstract class AbstractArtifactSpec(
    val kind: OperationKind,
    val type: Type,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val modifiers: Set<Modifier> = emptySet(),
    val tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> Any,
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"kind":"$kind","returnType":"$type","description":${description.quote()},"params":[""")
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
        fun createTombstone(original: AbstractArtifactSpec, tag: Long) = object : AbstractArtifactSpec(
            original.kind, original.type, original.name, "Tombstone for '${original.kind}:${original.name}'.", emptyList(), setOf(Modifier.DELETED),  tag
        , {
            throw UnsupportedOperationException("Tombstone for '${original.kind}:${original.name}' can't be instantiated.")
        }) {}
    }

    enum class Modifier {
         NO_SIMULATION, DELETED
    }
}

