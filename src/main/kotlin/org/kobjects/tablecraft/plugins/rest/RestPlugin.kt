package org.kobjects.tablecraft.plugins.rest

import org.kobjects.tablecraft.pluginapi.*

class RestPlugin(val model: ModelInterface) : Plugin {
    override val operationSpecs = listOf(
        OperationSpec(
            OperationKind.INTEGRATION,
            Type.VOID,
            "Rest",
            "Creates a REST JSON server",
            listOf(
                ParameterSpec("port", ParameterKind.CONFIGURATION, Type.INT, required = true)),
        ) {
            RestIntegration(it)
        }
    )
}