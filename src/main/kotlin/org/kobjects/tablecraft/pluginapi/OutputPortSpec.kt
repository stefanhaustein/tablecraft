package org.kobjects.tablecraft.pluginapi

class OutputPortSpec(
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any?>) -> OutputPortInstance,
) : AbstractFactorySpec(
    category,
    OperationKind.OUTPUT_PORT,
    name,
    description,
    parameters,
    modifiers,
    tag
)