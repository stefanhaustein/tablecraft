package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.quote

data class PortSpec(
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val tag: Long = 0,
    val createFn: (name: String, configuration: Map<String, Any>) -> PortInstance,
) {

    fun toJson(): String {
        val convertedParams = parameters.joinToString { it.toJson() }
        return """{"name":${name.quote()},"kind":"PORT_CONSTRUCTOR","description":${description.quote()},"params":[$convertedParams]}"""
    }
}