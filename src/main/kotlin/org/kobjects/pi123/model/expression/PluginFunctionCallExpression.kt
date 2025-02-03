package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.pluginapi.FunctionHost
import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.ParameterKind
import org.kobjects.pi123.pluginapi.Type

class PluginFunctionCallExpression(
    val cell: Cell,
    functionSpec: FunctionSpec,
    override val configuration: Map<String, Any>,
    val parameters: Map<String, Pair<Expression, Type>>
): Expression(), FunctionHost {

    override val children: Collection<Expression>
        get() = parameters.values.map { it.first }

    val functionInstance = functionSpec.createFn(this)

    override fun attach() {
        functionInstance.attach()
    }

    override fun detach() {
        functionInstance.detach()
    }

    override fun notifyValueChanged(newValue: Any) {
        Model.withLock {
            cell.updateAllDependencies(it)
            Model.notifyContentUpdated(it)
        }
    }

    override fun eval(context: RuntimeContext): Any {
        return functionInstance.apply(parameters.mapValues {
            val expr = it.value.first
            when (it.value.second) {
                Type.INT -> expr.evalInt(context)
                Type.NUMBER -> expr.evalDouble(context)
                Type.BOOLEAN -> expr.evalBoolean(context)
                Type.TEXT -> expr.eval(context).toString()
            }
        })
    }


    companion object {
        fun create(cell: Cell, functionSpec: FunctionSpec, parameters: Map<String, Expression>): PluginFunctionCallExpression {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Pair<Expression, Type>>()
            for ((index, specParam) in functionSpec.parameters.withIndex()) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                if (actualParameter != null) {
                    when (specParam.kind) {
                        ParameterKind.CONFIGURATION -> {
                            require(actualParameter is LiteralExpression) { "Literal expression required for configuration parameter ${specParam.name}" }
                            mappedConfig[specParam.name] = actualParameter.value!!
                        }
                        ParameterKind.RUNTIME -> mappedParameters[specParam.name] = actualParameter to specParam.type
                    }
                } else if (specParam.required) {
                    throw IllegalStateException("Parameter '${specParam.name}' not found in $parameters")
                }

            }
            return PluginFunctionCallExpression(cell, functionSpec, mappedConfig, mappedParameters)
        }
    }

}