package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*
import java.io.Writer


class Ports : Iterable<PortHolder> {

    private val portMap = mutableMapOf<String, PortHolder>()

    override fun iterator(): Iterator<PortHolder> = portMap.values.iterator()

    operator fun get(key: String): PortHolder? = portMap[key]

    val keys
        get() = portMap.keys

    fun deletePort(name: String, token: ModificationToken) {
        token.symbolsChanged = true
        portMap[name]?.detach()
        portMap[name] = InputPortHolder(name, InputPortSpec(
            Type.STRING,
            "TOMBSTONE",  // The operation name; used to identify tombstone ports on the client
            "",
            emptyList(),
            emptySet(),
            token.tag) {
            object : InputPortInstance {
                override fun attach(host: ValueChangeListener) {}
                override fun detach() {}
                override fun getValue() = Unit
            }
        }, emptyMap(), token.tag)
    }

    // The name is separate because it's typically the key of the spec map
    fun definePort(name: String?, jsonSpec: Map<String, Any>, token: ModificationToken) {
        token.symbolsChanged = true

        val previousName = jsonSpec["previousName"]

        if (previousName is String && !previousName.isNullOrBlank()) {
            try {
                deletePort(previousName, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!name.isNullOrBlank()) {
            val type = jsonSpec["type"].toString()

            portMap[name]?.detach()

            val specification = Model.functionMap[type]!!

            val config = specification.convertConfiguration(
                jsonSpec["configuration"] as Map<String, Any>)

            val port = when (specification) {
                is InputPortSpec -> InputPortHolder(name, specification, config, token.tag)
                is OutputPortSpec -> OutputPortHolder(name, specification, config, jsonSpec["expression"] as String, token.tag)
                else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
            }
            portMap[name] = port
            port.reset(Model.simulationMode_, token)
        }
    }

    fun serialize(writer: Writer, tag: Long) {
        val definitions = StringBuilder()
        val values = StringBuilder()
        val simulationValues = StringBuilder()
        for (port in this) {
            if (port.tag > tag) {
                definitions.append(port.name).append(": ")
                port.toJson(definitions)
                definitions.append('\n')
            }
            if (port.valueTag > tag) {
                values.append("${port.name}: ${port.value.toJson()}\n")
            }
            if (port is InputPortHolder && port.simulationValueTag > tag) {
                simulationValues.append("${port.name}: ${port.simulationValue.toJson()}\n")
            }
        }

        if (definitions.isNotEmpty()) {
            writer.write("[ports]\n\n$definitions\n")
        }
        if (values.isNotEmpty()) {
            writer.write("[portValues]\n\n$values\n")
        }

        if (simulationValues.isNotEmpty()) {
            writer.write("[simulationValues]\n\n$simulationValues\n")
        }
    }
}