package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.ExpressionNode
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.pluginapi.*

class PluginOperationCall(
    val owner: ExpressionNode,
    operationSpec: OperationSpec,
    override val configuration: Map<String, Any>,
    val parameters: Map<String, Pair<Expression, Type>>

): Expression(), OperationHost {

    override val children: Collection<Expression>
        get() = parameters.values.map { it.first }

    val operation = operationSpec.createFn(configuration) as Operation

    override fun attach() {
        if (operation is StatefulOperation) {
            operation.attach(this)
        }
    }

    override fun detach() {
        if (operation is StatefulOperation) {
            operation.detach()
        }
    }

    override fun notifyValueChanged(newValue: Any, token: ModificationToken) {
        token.addRefresh(owner)
    }

    override fun eval(context: EvaluationContext): Any {
        return operation.apply(parameters.mapValues {
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
        fun create(expressionHolder: ExpressionNode, operationSpec: OperationSpec, parameters: Map<String, Expression>): PluginOperationCall {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Pair<Expression, Type>>()
            for ((index, specParam) in operationSpec.parameters.withIndex()) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                if (actualParameter != null) {
                    when (specParam.kind) {
                        ParameterKind.CONFIGURATION -> {
                            require(actualParameter is Literal) { "Literal expression required for configuration parameter ${specParam.name}" }
                            mappedConfig[specParam.name] = actualParameter.value!!
                        }
                        ParameterKind.RUNTIME -> mappedParameters[specParam.name] = actualParameter to specParam.type
                    }
                } else if (specParam.required) {
                    throw IllegalStateException("Parameter '${specParam.name}' not found in $parameters")
                }

            }
            return PluginOperationCall(expressionHolder, operationSpec, mappedConfig, mappedParameters)
        }
    }

    override fun toString() = "$operation configuration: $configuration parameters: $parameters cell: $owner"

}