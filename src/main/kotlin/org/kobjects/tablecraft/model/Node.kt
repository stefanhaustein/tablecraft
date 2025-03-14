package org.kobjects.tablecraft.model

interface Node {
    val dependencies: MutableSet<Node>
    val dependsOn: MutableSet<Node>

    fun updateValue(tag: Long): Boolean
    fun detach()
}