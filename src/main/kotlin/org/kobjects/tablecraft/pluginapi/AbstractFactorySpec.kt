package org.kobjects.tablecraft.pluginapi

abstract class AbstractFactorySpec(
    category: String,
    kind: OperationKind,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    tag: Long
) : AbstractArtifactSpec(
    category,
    kind,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
)