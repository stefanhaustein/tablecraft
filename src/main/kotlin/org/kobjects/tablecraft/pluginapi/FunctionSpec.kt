package org.kobjects.tablecraft.pluginapi

class FunctionSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> Operation,
) : AbstractArtifactSpec(
    OperationKind.FUNCTION,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
)