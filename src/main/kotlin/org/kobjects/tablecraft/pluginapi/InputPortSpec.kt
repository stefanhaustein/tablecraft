package org.kobjects.tablecraft.pluginapi

class InputPortSpec(
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any?>) -> InputPortInstance,
) : AbstractFactorySpec(
    category,
    OperationKind.INPUT_PORT,
    name,
    description,
    parameters,
    modifiers,
    tag
)