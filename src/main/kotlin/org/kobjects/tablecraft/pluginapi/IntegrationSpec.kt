package org.kobjects.tablecraft.pluginapi

class IntegrationSpec(
    category: String,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (kind: String, name: String, tag: Long, initialConfiguration: Map<String, Any?>) -> IntegrationInstance,
) : AbstractFactorySpec(
    category,
    OperationKind.INTEGRATION,
    name,
    null,
    description,
    parameters,
    modifiers,
    tag,
)