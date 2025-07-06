package org.kobjects.tablecraft.pluginapi

class InputPortSpec(
    val category: String,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> InputPortInstance,
) : AbstractFactorySpec(
    OperationKind.INPUT_PORT,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag
)