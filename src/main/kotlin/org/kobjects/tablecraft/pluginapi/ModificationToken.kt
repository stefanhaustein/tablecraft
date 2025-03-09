package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.Expression

class ModificationToken() {

    val tag: Long = System.nanoTime()

    var loading = false
    var formulaChanged = false
    var functionSetChanged = false
    val refresh = mutableSetOf<Expression>()

    fun addRefresh(dependency: Expression) {
        if (refresh.add(dependency)) {
            for (child in dependency.dependencies) {
                addRefresh(child)
            }
        }
    }


}