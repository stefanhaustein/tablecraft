package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.model.RuntimeContext

class LiteralExpression(val value: Any) : Expression() {


    override fun eval(context: RuntimeContext) = value

    override val children: Collection<Expression>
        get() = emptyList()
}