package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class InputPortHolder(
    override val name: String,
    val specification: InputPortSpec,
    val configuration: Map<String, Any?>,
    override val tag: Long

) : PortHolder, Node, InputPortListener {

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()

    var instance: InputPortInstance? = null
    override var valueTag  = 0L

    override var value: Any? = null
    var portValue: Any? = null
    var simulationValue: Any? = null
    var simulationValueTag: Long = 0


    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        detach()

        if (!simulationMode) {
            try {
                instance = specification.createFn(configuration, this)
            } catch (e: Exception) {
                portValue = e
                e.printStackTrace()
            }
        }
    }

    override fun detach() {
        // This doesn't really need to do anything about dependencies -- dependecies will be updatend in their reset
        // methods.
        if (instance != null) {
            try {
                instance?.detach()
            } catch (e: Exception) {
                e.printStackTrace()
                instance = null
            }
        }
    }

    // Implements the corresponding value change listener method.
    override fun updateValue(token: ModificationToken, newValue: Any?) {
        Model.applySynchronizedWithToken {
            portValue = newValue
            it.addRefresh(this)
        }
    }

    // Overrides the corresponding Node method.
    override fun updateValue(token: ModificationToken): Boolean {
        if (valueTag == token.tag) {
            return false
        }
        val newValue = if (Model.simulationMode_) simulationValue else portValue
        if (value == newValue) {
            return false
        }
        valueTag = token.tag
        value = newValue
        return true
    }

    override fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "kind":${specification.name.quote()}, "type":""")
        specification.type.toJson(sb)
        sb.append(""", "configuration": """)
        configuration.toJson(sb)
        if (forClient) {
            serializeDependencies(sb)
        }
        sb.append("}")
    }

    fun setSimulationValue(value: Any?, token: ModificationToken) {
        simulationValue = value
        if (Model.simulationMode_) {
            token.addRefresh(this)
        } else {
            simulationValueTag = token.tag
        }
    }


    override fun toString() = name

}