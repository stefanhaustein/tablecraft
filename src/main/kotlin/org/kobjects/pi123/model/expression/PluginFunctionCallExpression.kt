package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.ParameterKind
import org.kobjects.pi123.pluginapi.Type

class PluginFunctionCallExpression(
    val cell: Cell,
    functionSpec: FunctionSpec,
    configuration: Map<String, Any>,
    val parameters: Map<String, Pair<Expression, Type>>
): Expression() {

    override val children: Collection<Expression>
        get() = parameters.values.map { it.first }

    val functionInstance = functionSpec.createFn(configuration) {
        update()
    }

    override fun attach() {
        functionInstance.attach()
    }

    override fun detach() {
        functionInstance.detach()
    }

    fun update() {
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
                Type.DOUBLE -> expr.evalDouble(context)
                Type.BOOLEAN -> expr.evalBoolean(context)
                Type.TEXT -> expr.eval(context).toString()
            }

        })
    }


    companion object {
        fun create(cell: Cell, functionSpec: FunctionSpec, parameters: Map<String, Expression>): PluginFunctionCallExpression {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Pair<Expression, Type>>()
            var index = 0
            for (specParam in functionSpec.parameters) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                require(actualParameter != null) { "Parameter ${specParam.name} not found in $parameters" }
                when (specParam.kind) {
                    ParameterKind.CONFIGURATION -> {
                        require(actualParameter is LiteralExpression) { "Literal expression required for configuration parameter ${specParam.name}" }
                        mappedConfig[specParam.name] = actualParameter.value!!
                    }
                    ParameterKind.RUNTIME -> mappedParameters[specParam.name] = actualParameter to specParam.type
                }
                index++
            }
            return PluginFunctionCallExpression(cell, functionSpec, mappedConfig, mappedParameters)
        }
    }

}