package org.kobjects.pi123.model.expression

abstract class Expression {

    abstract fun eval(): Any?

    fun evalDouble(): Double = eval() as Double
}