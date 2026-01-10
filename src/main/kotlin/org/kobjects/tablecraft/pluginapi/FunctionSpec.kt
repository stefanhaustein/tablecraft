package org.kobjects.tablecraft.pluginapi

class FunctionSpec(
    category: String,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    displayName: String? = null,
    val createFn: (configuration: Map<String, Any?>) -> FunctionInstance,
) : AbstractArtifactSpec(
    category,
    OperationKind.FUNCTION,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
    displayName,
)