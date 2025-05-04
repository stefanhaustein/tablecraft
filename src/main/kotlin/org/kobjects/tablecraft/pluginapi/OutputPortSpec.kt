package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.OutputPort

class OutputPortSpec(
    returnType: Type,
    name: String,
    description: String,
    parameters: List<ParameterSpec>,
    modifiers: Set<Modifier> = emptySet(),
    tag: Long = 0,
    val createFn: (configuration: Map<String, Any>) -> StatefulOperation,
) : AbstractArtifactSpec(
    OperationKind.OUTPUT_PORT,
    returnType,
    name,
    description,
    parameters,
    modifiers,
    tag
)