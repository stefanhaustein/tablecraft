package org.kobjects.tablecraft.pluginapi

class IntegrationSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> IntegrationInstance,
) : AbstractArtifactSpec(
    OperationKind.INTEGRATION,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
)