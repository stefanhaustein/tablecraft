package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote

interface Node {
    val value: Any
    val valueTag: Long
    val dependencies: MutableSet<Node>
    val inputs: MutableSet<Node>

    fun updateValue(tag: Long): Boolean
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