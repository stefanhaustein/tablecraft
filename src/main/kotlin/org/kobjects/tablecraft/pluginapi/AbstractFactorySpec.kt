package org.kobjects.tablecraft.pluginapi

abstract class AbstractFactorySpec(
    kind: OperationKind,
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier>,
    tag: Long
) : AbstractArtifactSpec(
    kind,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag,
)