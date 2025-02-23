package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote

data class ParameterSpec(
    val name: String,
    val kind: ParameterKind,
    val type: Type,
    val required: Boolean = false
) : ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"kind":${kind.toString().quote()},"type":${type.toString().quote()}}""")
    }
}