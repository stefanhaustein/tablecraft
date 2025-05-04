package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

data class ParameterSpec(
    val name: String,
    val type: Type,
    val modifiers: Set<Modifier> = emptySet()
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"modifiers":${modifiers.map { it.name }.toJson()}, "type":${type.toJson()}}""")
    }

    enum class Modifier {
        CONSTANT, OPTIONAL, REFERENCE
    }
}