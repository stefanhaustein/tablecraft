package org.kobjects.tablecraft.pluginapi

abstract class AbstractFactorySpec(
    category: String,
    kind: OperationKind,
    name: String,
    type: Type?,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    tag: Long
) : AbstractArtifactSpec(
    category,
    kind,
    type,
    name,
    description,
    parameters,
    modifiers,
    tag,
)