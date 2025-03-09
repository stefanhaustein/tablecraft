package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class OutputPort(
    name: String,
    val specification: OperationSpec,
    override val configuration: Map<String, Any>,
    val rawExpression: String,
    token: ModificationToken
) : Port(name, token.tag) {
    val portOperation = specification.createFn(this)
    val dependencies = mutableSetOf<Dependency>()
    var error: Exception? = null
    val expression: Expression = Expression()
    var attached = false

    init {
        expression.changeListeners.add {
            val parameters = mapOf("value" to it)
            for (dependency in dependencies) {
                dependency.apply(parameters)
            }
            if (attached) {
                portOperation.apply(parameters)
            }
        }
        Model.functionMap[name] = OperationSpec(
            OperationKind.FUNCTION,
            specification.returnType,
            name,
            specification.name + configuration,
            listOf(ParameterSpec("value", ParameterKind.RUNTIME, specification.returnType)),
            tag
        ) {
            Dependency(it)
        }
    }

    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        expression.setFormula(rawExpression, token)
        if (attached) {
            try {
                portOperation.detach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            attached = false
        }

        if (!simulationMode) {
            try {
                portOperation.attach()
                attached = true
            } catch (exception: Exception) {
                error = exception
                exception.printStackTrace()
            }
        }
    }

    override fun detach() {}

    override fun notifyValueChanged(newValue: Any, token: ModificationToken) {
        System.out.println("Unexpected change notification in Output Port")
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append(""", "expression":${expression.rawValue.quote()}}""")

    }

    inner class Dependency(val host: OperationHost) : OperationInstance {
        override fun attach() {
            dependencies.add(this)
        }

        override fun apply(params: Map<String, Any>): Any {
            return expression.computedValue_
        }

        override fun detach() {
            dependencies.remove(this)
        }

    }
}