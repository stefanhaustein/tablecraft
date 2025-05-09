package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class OutputPortHolder(
    override val name: String,
    val specification: OutputPortSpec,
    val configuration: Map<String, Any>,
    override val rawFormula: String,
    override val tag: Long
) : ExpressionNode(), PortHolder {
    val instance = specification.createFn(configuration)
    var error: Exception? = null
    var attached = false

    override fun updateValue(tag: Long): Boolean =
        if (super.updateValue(tag)) {
            if (attached) {
                try {
                    instance.setValue(value)
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

        if (!simulationMode || specification.modifiers.contains(AbstractArtifactSpec.Modifier.NO_SIMULATION)) {
            try {
                instance.attach()
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
                instance.detach()
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
        serializeDependencies(sb)

        sb.append(""", "source":${rawFormula.quote()}}""")

    }


    override fun toString() = name
}