package org.kobjects.pi123.model.expression

import kotlinx.datetime.Instant
import org.kobjects.pi123.model.RuntimeContext

class BinaryOperatorExpression(
    val name: String,
    val leftOperand: Expression,
    val rightOperand: Expression
): Expression() {

    override fun eval(context: RuntimeContext): Any {
        val l = leftOperand.eval(context) ?: 0.0
        return when (l) {
            is Double,
            is Instant -> {
                val l = toDouble(l)
                val r = rightOperand.evalDouble(context)
                when (name) {
                    "+" -> l + r
                    "-" -> l - r
                    "*" -> l * r
                    "/" -> l / r
                    else -> throw UnsupportedOperationException("$name for Double")
                }
            }

            else -> throw UnsupportedOperationException("Type")
        }
    }

    override val children: Collection<Expression>
        get() = listOf(leftOperand, rightOperand)
}