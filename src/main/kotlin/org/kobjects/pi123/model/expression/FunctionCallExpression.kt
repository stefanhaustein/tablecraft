package org.kobjects.pi123.model.expression

class FunctionCallExpression(
    val name: String,
    val parameters: Map<String, Expression>) : Expression() {

    override fun eval(): Any {
        return when (name) {
            "pi" -> Math.PI
            "now" -> return System.currentTimeMillis().toDouble() / 86400000.0
            else -> throw UnsupportedOperationException(name)
        }
    }

    override val children: Collection<Expression>
        get() = parameters.values

}