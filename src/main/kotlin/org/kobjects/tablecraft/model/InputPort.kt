package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.Model.simulationValueMap
import org.kobjects.tablecraft.pluginapi.*

class InputPort(
    name: String,
    val specification: OperationSpec,
    override val configuration: Map<String, Any>,
    token: ModificationToken

) : Port(name, token.tag), Dependable {

    override val dependencies = mutableSetOf<Expression>()

    val portOperation = specification.createFn(this)
    var error: Exception? = null
    var attached: Boolean = false

    private var value_: Any = when(specification.returnType) {
        Type.INT -> 0
        Type.NUMBER -> 0.0
        Type.BOOLEAN -> false
        Type.TEXT -> ""
        else -> throw UnsupportedOperationException("port type")
    }


    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        if (attached) {
            try {
                portOperation.detach()
            } catch (e: Exception) {
                e.printStackTrace()
                attached = false
            }
        }

        if (!simulationMode) {
            try {
                portOperation.attach()
                attached = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun detach() {}

    override val value: Any
        get() = if (Model.simulationMode_) simulationValueMap[name] ?: Unit else value_

    // Incoming from ports
    override fun notifyValueChanged(newValue: Any, token: ModificationToken) {
        if (!Model.simulationMode_) {
            if (value_ == newValue) {
                return
            }
            value_ = newValue
        }
        for (dependency in dependencies) {
            token.addRefresh(dependency)
        }
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append("}")
    }

}