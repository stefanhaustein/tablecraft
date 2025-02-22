package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.quote

data class PortSpec(
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val createFn: (name: String, configuration: Map<String, Any>, tag: Long) -> PortInstance,
) {

    fun toJson(): String {
        val convertedParams = parameters.joinToString { it.toJson() }
        return """{"name":${name.quote()},"kind":"PORT_CONSTRUCTOR","description":${description.quote()},"params":[$convertedParams]}"""
    }


}