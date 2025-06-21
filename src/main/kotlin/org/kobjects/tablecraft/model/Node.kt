package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.pluginapi.ModificationToken

interface Node {
    val value: Any
    val valueTag: Long
    val dependencies: MutableSet<Node>
    val inputs: MutableSet<Node>

    fun updateValue(token: ModificationToken): Boolean
    fun detach()

    fun qualifiedId(): String

    fun serializeDependencies(sb: StringBuilder) {
        if (inputs.isNotEmpty()) {
            sb.append(""", "inputs":[${inputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
        if (dependencies.isNotEmpty()) {
            sb.append(""", "dependencies":[${dependencies.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
    }

}