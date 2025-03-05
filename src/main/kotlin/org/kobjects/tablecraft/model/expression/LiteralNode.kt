package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.RuntimeContext

class LiteralNode(val value: Any) : Node() {


    override fun eval(context: RuntimeContext) = value

    override val children: Collection<Node>
        get() = emptyList()
}