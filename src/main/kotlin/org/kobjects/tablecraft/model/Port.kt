package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.model.expression.PortReference
import org.kobjects.tablecraft.pluginapi.*

interface Port: OperationHost, ToJson, Node {

    val name: String
    val tag: Long

    fun reset(simulationMode: Boolean, token: ModificationToken)

    override fun qualifiedId() = name

    override fun equivalentNodes(): Set<Node> {
        val nodes = mutableSetOf<Node>(this)
        nodes.addAll(inputs)
        for (node in dependencies) {
            if (node is Cell && node.expression is PortReference) {
                nodes.add(node)
            }
        }
        return nodes.toSet()
    }
}