package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson

interface Integration : ToJson {
    val operationSpecs: List<OperationSpec>
    val tag: Long
    val type: String

    fun detach()

    class Tombstone(override val tag: Long) : Integration {

            override val operationSpecs = emptyList<OperationSpec>()
            override val type: String
                get() = "TOMBSTONE"

            override fun detach() {
            }

            override fun toJson(sb: StringBuilder) {
                sb.append("""{"type":"TOMBSTONE"}""")
            }

        }

}