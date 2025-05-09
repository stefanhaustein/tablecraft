package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

abstract class IntegrationInstance(
    val kind: String,
    val name: String,
    val tag: Long,
) : ToJson {

    abstract val operationSpecs: List<AbstractFactorySpec>

    abstract val configuration: Map<String, Any>

    abstract fun detach()

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${kind.quote()}, "configuration": """)
        configuration.toJson(sb)
        sb.append("}")
    }

    abstract fun reconfigure(configuration: Map<String, Any>)

    class Tombstone(
        deletedInstance: IntegrationInstance,
        tag: Long
    ) : IntegrationInstance(
        "TOMBSTONE",
        deletedInstance.name,
        tag
    ) {
        override val configuration = emptyMap<String, Any>()
        override fun reconfigure(configuration: Map<String, Any>) {}
        override val operationSpecs = emptyList<AbstractFactorySpec>()

        override fun detach() {}
    }

}