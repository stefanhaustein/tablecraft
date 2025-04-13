package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class OutputPort(
    override val name: String,
    val specification: OperationSpec,
    val configuration: Map<String, Any>,
    override val rawFormula: String,
    override val tag: Long
) : ExpressionNode(), Port {
    val portOperation = specification.createFn(configuration) as StatefulOperation
    var error: Exception? = null
    var attached = false

    override fun updateValue(tag: Long): Boolean =
        if (super.updateValue(tag)) {
            if (attached) {
                try {
                    val parameters = mapOf("value" to value)
                    portOperation.apply(parameters)
                    error = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    error = e
                }
            }
            true
        } else false


    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        detach()

        reparse()

        if (!simulationMode) {
            try {
                portOperation.attach(this)
                attached = true
            } catch (exception: Exception) {
                error = exception
                exception.printStackTrace()
            }
        }
    }

    override fun detach() {
        super.detach()

        if (attached) {
            try {
                portOperation.detach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            attached = false
        }
    }

    override fun notifyValueChanged(token: ModificationToken) {
        System.out.println("Unexpected change notification in Output Port")
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append(""", "dependsOn":${inputs.toString().quote()}""")
        sb.append(""", "dependencies":${dependencies.toString().quote()}""")

        sb.append(""", "expression":${rawFormula.quote()}}""")

    }


    override fun toString() = name
}