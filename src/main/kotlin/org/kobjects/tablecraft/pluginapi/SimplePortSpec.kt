package org.kobjects.tablecraft.pluginapi

import javax.sound.sampled.Port

fun SimplePortSpec(
    name: String,
    description: String,
    configuration: List<ParameterSpec>,
    returnType: Type,
    parameters: List<ParameterSpec>,
    operationCreateFn:  (host: OperationHost) -> OperationInstance
) = PortSpec(
    name,
    description,
    configuration
) {  instanceName, instanceConfiguration, tag ->
    object : PortInstance {
        override val type = name
        override val name = instanceName
        override val tag = tag
        override val configuration = instanceConfiguration
        override val operationSpecs = listOf(
            OperationSpec(
                if (parameters.isEmpty()) OperationKind.INPUT_PORT else OperationKind.FUNCTION,
                returnType,
                instanceName,
                description,
                parameters,
                tag
            ) {
                operationCreateFn(object : OperationHost {
                    override val configuration = instanceConfiguration + it.configuration
                    override fun notifyValueChanged(newValue: Any) = it.notifyValueChanged(newValue)
                })
            }
        )
    }
}
