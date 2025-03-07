package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.Model.simulationValueMap
import org.kobjects.tablecraft.pluginapi.*

class Port(
    val name: String,
    val bindingName: String,
    override val configuration: Map<String, Any>,
    val tag: Long = 0,
) : OperationHost, ToJson {

    val specification = Model.functionMap[bindingName]!!
    val implementation = specification.createFn(this)
    val dependencies = mutableSetOf<PortDependency>()
    var error: Exception? = null
    var expression: Expression? = null

    private var value_: Any = (if (Model.simulationMode_) Model.simulationValueMap[name] else null) ?:
        when(specification.returnType) {
            Type.INT -> 0
            Type.NUMBER -> 0.0
            Type.BOOLEAN -> false
            Type.TEXT -> ""
            else -> throw UnsupportedOperationException("port type")
        }

    init {
        try {
            implementation.attach()
        } catch (exception: Exception) {
            error = exception
            exception.printStackTrace()
        }
        Model.functionMap[name] = OperationSpec(
            OperationKind.FUNCTION,
            specification.returnType,
            name,
            bindingName + configuration,
            if (specification.kind == OperationKind.INPUT_PORT) emptyList()
            else listOf(ParameterSpec("value", ParameterKind.RUNTIME, specification.returnType)),
            tag
        ) {
            PortDependency(it)
        }
    }

    val value: Any
        get() = if (Model.simulationMode_) simulationValueMap[name] ?: Unit else value_


    fun setExpression(rawExpression: String, runtimeContext: RuntimeContext?) {
        val expression = Expression()
        expression.setFormula(rawExpression, runtimeContext)
        expression.changeListeners.add {
            notifyValueChanged(expression.computedValue_)
        }
        this.expression = expression
    }

    // Incoming from ports
    override fun notifyValueChanged(newValue: Any) {
        if (Model.simulationMode_) {
            for (adapter in dependencies) {
                adapter.host.notifyValueChanged(value)
            }
        } else {
            if (value_ != newValue) {
                value_ = newValue
                for (adapter in dependencies) {
                    adapter.host.notifyValueChanged(value)
                }
            }
        }
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${bindingName.quote()}, "configuration": """)
        configuration.toJson(sb)
        if (expression != null) {
            sb.append(""", "expression":${expression!!.rawValue.quote()}""")
        }

        sb.append("}")
    }

    inner class PortDependency(val host: OperationHost) : OperationInstance {
        override fun attach() {
            dependencies.add(this)
        }

        override fun apply(params: Map<String, Any>): Any {
            return value
        }

        override fun detach() {
            dependencies.remove(this)
        }

    }

}