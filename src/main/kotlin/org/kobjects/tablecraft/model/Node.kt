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
    fun equivalentNodes(): Set<Node>

    fun serializeDependencies(sb: StringBuilder) {
        val eq = equivalentNodes()
        val saturatedInputs = mutableSetOf<Node>()
        val saturatedDependencies = mutableSetOf<Node>()
        for (node in eq) {
            for (dependency in node.dependencies) {
                saturatedDependencies.addAll(dependency.equivalentNodes())
            }
            for (input in node.inputs) {
                saturatedInputs.addAll(input.equivalentNodes())
            }
        }
        saturatedInputs.removeAll(eq)
        saturatedDependencies.removeAll(eq)

        val otherEq = eq.filter { it != this }
        if (otherEq.isNotEmpty()) {
            sb.append(""", "equivalent":[${otherEq.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
        if (saturatedInputs.isNotEmpty()) {
            sb.append(""", "inputs":[${saturatedInputs.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
        if (saturatedDependencies.isNotEmpty()) {
            sb.append(""", "dependencies":[${saturatedDependencies.joinToString(",") {
                it.qualifiedId().quote() }}]""")
        }
    }

}