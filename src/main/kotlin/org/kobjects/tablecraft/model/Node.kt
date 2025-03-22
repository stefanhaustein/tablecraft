package org.kobjects.tablecraft.model

interface Node {
    val value: Any
    val valueTag: Long
    val dependencies: MutableSet<Node>
    val inputs: MutableSet<Node>

    fun updateValue(tag: Long): Boolean
    fun detach()

    fun qualifiedId(): String
    fun equivalentNodes(): Set<Node>
}