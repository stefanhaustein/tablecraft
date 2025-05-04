package org.kobjects.tablecraft.pluginapi

class FunctionSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    createFn: (configuration: Map<String, Any>) -> Any,
) : AbstractArtifactSpec(
    OperationKind.FUNCTION,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
    createFn
)