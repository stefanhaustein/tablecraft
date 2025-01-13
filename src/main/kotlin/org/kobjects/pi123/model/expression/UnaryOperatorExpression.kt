package org.kobjects.pi123.model.expression

class UnaryOperatorExpression(val name: String, val operand: Expression) : Expression() {

    override fun eval(): Any {
        val value = operand.eval()
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