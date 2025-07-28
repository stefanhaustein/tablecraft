package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.pluginapi.ModificationToken

interface Node {
    val value: Any?
    val valueTag: Long
    val outputs: MutableSet<Node>
    val inputs: MutableSet<Node>

    /**
     * Re-calculates the value bases on inputs.
     * Input port values will be refreshed from the port/simulation
     * value here.
     */
    fun updateValue(token: ModificationToken): Boolean
    fun detach()

    fun qualifiedId(): String

    fun serializeDependencies(sb: StringBuilder) {
        if (inputs.isNotEmpty()) {
            sb.append(""", "inputs":[${inputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
        if (outputs.isNotEmpty()) {
            sb.append(""", "outputs":[${outputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
    }

}