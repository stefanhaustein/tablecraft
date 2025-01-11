package org.kobjects.pi123.model.expression

class LiteralExpression(val value: Any) : Expression() {


    override fun eval() = value
}