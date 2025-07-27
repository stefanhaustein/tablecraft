package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class InputPortHolder(
    override val name: String,
    val specification: InputPortSpec,
    val configuration: Map<String, Any?>,
    override val tag: Long

) : PortHolder, Node, ValueReceiver {

    override val outputs = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()

    val instance = specification.createFn(configuration)
    var error: Exception? = null
    var attached: Boolean = false
    override var valueTag  = 0L

    override var value: Any? = null
    var simulationValue: Any? = value
    var simulationValueTag: Long = 0


    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        detach()

        if (!simulationMode) {
            try {
                instance.attach(this)
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
                instance.detach()
            } catch (e: Exception) {
                e.printStackTrace()
                attached = false
            }
        }
    }

    override fun updateValue(newValue: Any?) {
        Model.applySynchronizedWithToken {
            value = newValue
            it.addRefresh(this)
        }
    }

    override fun updateValue(token: ModificationToken): Boolean {
        if (valueTag == token.tag) {
            return false
        }
        val newValue = if (Model.simulationMode_) simulationValue else instance.getValue()
        if (value == newValue) {
            return false
        }
        valueTag = token.tag
        value = newValue
        return true
    }

    override fun toJson(sb: StringBuilder, forClient: Boolean) {
        sb.append("""{"name":${name.quote()}, "kind":${specification.name.quote()}, "type":""")
        instance.type.toJson(sb)
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