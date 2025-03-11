package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.ExpressionNode

class ModificationToken() {

    val tag: Long = System.nanoTime()

    var loading = false
    var formulaChanged = false
    var functionSetChanged = false
    val refreshRoots = mutableSetOf<ExpressionNode>()
    val refreshNodes = mutableSetOf<ExpressionNode>()

    fun addRefresh(expressionNode: ExpressionNode) {
        if (expressionNode.dependsOn.isEmpty()) {
            refreshRoots.add(expressionNode)
        } else {
            refreshNodes.add(expressionNode)
        }
    }

    fun addAllDependencies(node: ExpressionNode) {
        for (dep in node.dependencies) {
            if (refreshNodes.add(dep)) {
                addAllDependencies(dep)
            }
        }
    }

}