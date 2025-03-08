package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.pluginapi.ModificationToken

class UnaryOperatorNode(val name: String, val operand: Node) : Node() {

    override fun eval(context: ModificationToken): Any {
        val value = operand.eval(context)
        return when (value) {
            is Double -> {
                when (name) {
                    "-" -> -value
                    else -> throw UnsupportedOperationException("")
                }
            }
            else -> throw UnsupportedOperationException("")
        }
    }

    override val children: Collection<Node>
        get() = emptyList()
}