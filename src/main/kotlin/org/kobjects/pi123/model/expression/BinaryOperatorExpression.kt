package org.kobjects.pi123.model.expression

class BinaryOperatorExpression(
    val name: String,
    val leftOperand: Expression,
    val rightOperand: Expression
): Expression() {

    override fun eval(): Any {
        val l = leftOperand.eval()
        return when (l) {
            is Double -> {
                val r = rightOperand.evalDouble()
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
}