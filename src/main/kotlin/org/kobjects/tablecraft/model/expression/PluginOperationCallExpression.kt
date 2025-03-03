package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.RuntimeContext
import org.kobjects.tablecraft.pluginapi.*

class PluginOperationCallExpression(
    val cell: Cell,
    val operationSpec: OperationSpec,
    override val configuration: Map<String, Any>,
    val parameters: Map<String, Pair<Expression, Type>>
): Expression(), OperationHost {

    override val children: Collection<Expression>
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
            cell.updateAllDependencies(it)
            Model.notifyContentUpdated(it)
        }
    }

    override fun eval(context: RuntimeContext): Any {
        if (Model.simulationMode_) {
            val value = Model.simulationValueMap[operationSpec.name]
            if (value != null) {
                return value
            }
        }
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
        fun create(cell: Cell, operationSpec: OperationSpec, parameters: Map<String, Expression>): PluginOperationCallExpression {
            val mappedConfig = mutableMapOf<String, Any>()
            val mappedParameters = mutableMapOf<String, Pair<Expression, Type>>()
            for ((index, specParam) in operationSpec.parameters.withIndex()) {
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
            return PluginOperationCallExpression(cell, operationSpec, mappedConfig, mappedParameters)
        }
    }

    override fun toString() = "$operationInstance configuration: $configuration parameters: $parameters cell: $cell"

}