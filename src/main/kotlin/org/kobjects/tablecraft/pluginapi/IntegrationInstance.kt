package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson

abstract class IntegrationInstance(
    val config: Map<String, Any>,
) : ToJson {
    val name = config["name"] as String
    val tag = config["tag"] as Long

    abstract val operationSpecs: List<AbstractArtifactSpec>

    abstract val type: String

    abstract fun detach()

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${type.quote()}, "configuration": """)
        config.filterKeys { it != "name" && it != "tag" }.toJson(sb)
        sb.append("}")
    }

    class Tombstone(tag: Long) : IntegrationInstance(mapOf("tag" to tag)) {

            override val operationSpecs = emptyList<AbstractArtifactSpec>()
            override val type: String
                get() = "TOMBSTONE"

            override fun detach() {
            }

            override fun toJson(sb: StringBuilder) {
                sb.append("""{"type":"TOMBSTONE"}""")
            }

        }

}