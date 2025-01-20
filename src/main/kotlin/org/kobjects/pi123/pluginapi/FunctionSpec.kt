package org.kobjects.pi123.pluginapi

data class FunctionSpec(
    val name: String,
    val parameters: List<ParameterSpec>,
    val createFn: (configuration: Map<String, Any>, listener: ((Any) -> Unit)?) -> FunctionInstance)

