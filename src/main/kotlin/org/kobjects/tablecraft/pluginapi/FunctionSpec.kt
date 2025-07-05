package org.kobjects.tablecraft.pluginapi

class FunctionSpec(
    val category: String,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> FunctionInstance,
) : AbstractArtifactSpec(
    OperationKind.FUNCTION,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
)