package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.RuntimeContext

class BuiltinFunctionCallExpression(
    val name: String,
    val parameters: Map<String, Expression>) : Expression() {

    override fun eval(context: RuntimeContext): Any {
        return when (name) {
            "pi" -> Math.PI
            else -> throw UnsupportedOperationException(name)
        }
    }

    override val children: Collection<Expression>
        get() = parameters.values

}