package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.RuntimeContext

class UnaryOperatorExpression(val name: String, val operand: Expression) : Expression() {

    override fun eval(context: RuntimeContext): Any {
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

    override val children: Collection<Expression>
        get() = emptyList()
}