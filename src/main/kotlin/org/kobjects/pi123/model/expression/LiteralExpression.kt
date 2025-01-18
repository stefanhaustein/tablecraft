package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.RuntimeContext

class LiteralExpression(val value: Any?) : Expression() {


    override fun eval(context: RuntimeContext) = value

    override val children: Collection<Expression>
        get() = emptyList()
}