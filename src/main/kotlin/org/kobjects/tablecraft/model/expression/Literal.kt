package org.kobjects.tablecraft.model.expression

class Literal(val value: Any) : Expression() {


    override fun eval(context: EvaluationContext) = value

    override val children: Collection<Expression>
        get() = emptyList()
}