package org.kobjects.tablecraft.pluginapi

abstract class AbstractFactorySpec(
    category: String,
    kind: OperationKind,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    tag: Long
) : AbstractArtifactSpec(
    category,
    kind,
    null,
    name,
    description,
    parameters,
    modifiers,
    tag,
)