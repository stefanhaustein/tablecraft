package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.pluginapi.ModificationToken

class LiteralNode(val value: Any) : Node() {


    override fun eval(context: ModificationToken) = value

    override val children: Collection<Node>
        get() = emptyList()
}