package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.Expression
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.RuntimeContext
import org.kobjects.tablecraft.pluginapi.*

class PluginOperationCallNode(
    val expressionHolder: Expression,
    val operationSpec: OperationSpec,
    override val configuration: Map<String, Any>,
    val parameters: Map<String, Pair<Node, Type>>
): Node(), OperationHost {

    override val children: Collection<Node>
        get() = parameters.values.map { it.first }

    val operationInstance = operationSpec.createFn(this) as OperationInstance

    override fun attach() {

        operationInstance.attach()
    }

    override fun detach() {

        operationInstance.detach()
    }

    override fun notifyValueChanged(newValue: Any) {
        Model.withLock {
            expressionHolder.updateAllDependencies(it)
            Model.notifyContentUpdated(it)
        }
    }

    override fun eval(context: RuntimeContext): Any {
        return operationInstance.apply(parameters.mapValues {
            val expr = it.value.first
            when (it.value.second) {
                Type.INT -> expr.evalInt(context)
                Type.NUMBER -> expr.evalDouble(context)
                Type.BOOLEAN -> expr.evalBoolean(context)
                Type.TEXT -> expr.eval(context).toString()
                else -> expr.eval(context)
            }
        })
    }


    companion object {
        fun create(expressionHolder: Expression, operationSpec: OperationSpec, parameters: Map<String, Node>): PluginOperationCallNode {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Pair<Node, Type>>()
            for ((index, specParam) in operationSpec.parameters.withIndex()) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                if (actualParameter != null) {
                    when (specParam.kind) {
                        ParameterKind.CONFIGURATION -> {
                            require(actualParameter is LiteralNode) { "Literal expression required for configuration parameter ${specParam.name}" }
                            mappedConfig[specParam.name] = actualParameter.value!!
                        }
                        ParameterKind.RUNTIME -> mappedParameters[specParam.name] = actualParameter to specParam.type
                    }
                } else if (specParam.required) {
                    throw IllegalStateException("Parameter '${specParam.name}' not found in $parameters")
                }

            }
            return PluginOperationCallNode(expressionHolder, operationSpec, mappedConfig, mappedParameters)
        }
    }

    override fun toString() = "$operationInstance configuration: $configuration parameters: $parameters cell: $expressionHolder"

}