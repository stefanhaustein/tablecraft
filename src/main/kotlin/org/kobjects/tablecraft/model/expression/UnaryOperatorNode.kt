package org.kobjects.tablecraft.model.expression

class UnaryOperatorNode(val name: String, val operand: Node) : Node() {

    override fun eval(context: EvaluationContext): Any {
        val value = operand.eval(context)
        return when (value) {
            is Double -> when (name) {
                    "-" -> -value
                    else -> throw UnsupportedOperationException("$name not supported for Number operants.")
                }
            is Int -> when (name) {
                    "-" -> -value
                    else -> throw UnsupportedOperationException("$name not supported for Int operands.")
                }
            is Boolean -> when (name) {
                "not" -> !value
                else -> throw UnsupportedOperationException("$name not supported for Boolean operands.")
            }
            else -> throw UnsupportedOperationException("Unary operantions (here, '$name') are not supported for type ${value::class}")
        }
    }

    override val children: Collection<Node>
        get() = emptyList()
}