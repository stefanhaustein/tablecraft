package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote

data class PortSpec(
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val createFn: (name: String, configuration: Map<String, Any>, tag: Long) -> PortInstance,
): ToJson {

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()},"kind":"PORT_CONSTRUCTOR","description":${description.quote()},"params":[""")
        var first = true
        for (param in parameters) {
            if (first) {
                first = false
            } else {
                sb.append(",")
            }
            param.toJson(sb)
        }
        sb.append("]}")
    }
}