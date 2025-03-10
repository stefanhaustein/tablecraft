package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.Expression

class ModificationToken() {

    val tag: Long = System.nanoTime()

    var loading = false
    var formulaChanged = false
    var functionSetChanged = false
    val refreshRoots = mutableSetOf<Expression>()
    val refreshNodes = mutableSetOf<Expression>()

    fun addRefresh(expression: Expression) {
        if (expression.dependsOn.isEmpty()) {
            refreshRoots.add(expression)
        } else {
            refreshNodes.add(expression)
        }
    }

    fun addAllDependencies(node: Expression) {
        for (dep in node.dependencies) {
            if (refreshNodes.add(dep)) {
                addAllDependencies(dep)
            }
        }
    }

}