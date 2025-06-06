package org.kobjects.tablecraft.pluginapi

class IntegrationSpec(
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (kind: String, name: String, tag: Long, initialConfiguration: Map<String, Any>) -> IntegrationInstance,
) : AbstractFactorySpec(
    OperationKind.INTEGRATION,
    Type.VOID,
    name,
    description,
    parameters,
    modifiers,
    tag,
)