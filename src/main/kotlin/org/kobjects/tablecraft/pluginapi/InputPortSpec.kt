package org.kobjects.tablecraft.pluginapi

class InputPortSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> InputPortInstance,
) : AbstractArtifactSpec(
    OperationKind.INPUT_PORT,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag
)