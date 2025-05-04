package org.kobjects.tablecraft.pluginapi

class OutputPortSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> OutputPortInstance,
) : AbstractArtifactSpec(
    OperationKind.OUTPUT_PORT,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag
)