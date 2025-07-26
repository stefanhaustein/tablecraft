package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

data class ParameterSpec(
    val name: String,
    val type: Type,
    val defaultValue: Any?,
    val modifiers: Set<Modifier> = emptySet()
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":""")
        type.toJson(sb)
        if (defaultValue != null) {
            sb.append(""", "default":""")
            defaultValue.toJson(sb)
        }
        if (modifiers.isNotEmpty()) {
            sb.append(""", "modifiers":${modifiers.map { it.name }.toJson()}""")
        }
        sb.append("}")
    }

    enum class Modifier {
        CONSTANT, OPTIONAL, REFERENCE
    }
}