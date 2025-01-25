package org.kobjects.pi123.pluginapi

import org.kobjects.pi123.model.quote

data class FunctionSpec(
    val name: String,
    val parameters: List<ParameterSpec>,
    val createFn: (configuration: Map<String, Any>, listener: ((Any) -> Unit)?) -> FunctionInstance
) {

    fun toJson() =
        """{"name":${name.quote()},"params":[${parameters.joinToString { it.toJson() }}]}"""

}

