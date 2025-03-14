package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.ExpressionNode
import org.kobjects.tablecraft.model.Node

class ModificationToken() {

    val tag: Long = System.nanoTime()

    var loading = false
    var formulaChanged = false
    var symbolsChanged = false
    val refreshRoots = mutableSetOf<Node>()
    val refreshNodes = mutableSetOf<Node>()

    fun addRefresh(node: Node) {
        if (node.dependsOn.isEmpty()) {
            refreshRoots.add(node)
        } else {
            refreshNodes.add(node)
        }
    }

    fun addAllDependencies(node: Node) {
        for (dep in node.dependencies) {
            if (refreshNodes.add(dep)) {
                addAllDependencies(dep)
            }
        }
    }

}