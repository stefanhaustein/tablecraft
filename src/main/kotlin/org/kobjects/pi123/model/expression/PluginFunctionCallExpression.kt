package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.ParameterKind

class PluginFunctionCallExpression(
    val cell: Cell,
    functionSpec: FunctionSpec,
    configuration: Map<String, Any>,
    val parameters: Map<String, Expression>
): Expression() {

    override val children: Collection<Expression>
        get() = parameters.values

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
        return functionInstance.apply(parameters.mapValues {  it.value.eval(context)!! })
    }


    companion object {
        fun create(cell: Cell, functionSpec: FunctionSpec, parameters: Map<String, Expression>): PluginFunctionCallExpression {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Expression>()
            var index = 0
            for (specParam in functionSpec.parameters) {
                val actualParameter = parameters[specParam.name] ?: parameters["$index"]
                require(actualParameter != null) { "Parameter ${specParam.name} not found in $parameters" }
                when (specParam.kind) {
                    ParameterKind.CONFIGURATION -> {
                        require(actualParameter is LiteralExpression) { "Literal expression required for configuration parameter ${specParam.name}" }
                        mappedConfig[specParam.name] = actualParameter.value!!
                    }
                    ParameterKind.RUNTIME -> mappedParameters[specParam.name] = actualParameter
                }
                index++
            }
            return PluginFunctionCallExpression(cell, functionSpec, mappedConfig, mappedParameters)
        }
    }

}