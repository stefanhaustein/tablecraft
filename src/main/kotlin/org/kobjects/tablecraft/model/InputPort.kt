package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class InputPort(
    override val name: String,
    val specification: OperationSpec,
    val configuration: Map<String, Any>,
    override val tag: Long

) : Port, Node {

    override val dependencies = mutableSetOf<Node>()
    override val dependsOn = mutableSetOf<Node>()

    val portOperation = specification.createFn(configuration) as StatefulOperation
    var error: Exception? = null
    var attached: Boolean = false
    override var valueTag  = 0L

    override var value: Any = when(specification.returnType) {
        Type.INT -> 0
        Type.NUMBER -> 0.0
        Type.BOOLEAN -> false
        Type.TEXT -> ""
        else -> throw UnsupportedOperationException("port type")
    }
    var simulationValue: Any = value
    var simulationValueTag: Long = 0


    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        detach()

        if (!simulationMode) {
            try {
                portOperation.attach(this)
                attached = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun detach() {
        // This doesn't really need to do anything about dependencies -- dependecies will be updatend in their reset
        // methods.
        if (attached) {
            try {
                portOperation.detach()
            } catch (e: Exception) {
                e.printStackTrace()
                attached = false
            }
        }
    }


    // Incoming from ports
    override fun notifyValueChanged(token: ModificationToken) {
        token.addRefresh(this)
    }


    override fun updateValue(tag: Long): Boolean {
        if (valueTag == tag) {
            return false
        }
        val newValue = if (Model.simulationMode_) simulationValue else portOperation.apply(emptyMap())
        if (value == newValue) {
            return false
        }
        valueTag = tag
        value = newValue
        return true
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append("}")
    }

    fun setSimulationValue(value: Any, token: ModificationToken) {
        if (Model.simulationMode_) {
            notifyValueChanged(token)
        } else {
            simulationValue = value
            simulationValueTag = token.tag
        }
    }


    override fun toString() = name

}