package org.kobjects.pi123.model.expression

abstract class Expression {

    abstract fun eval(): Any?

    abstract val children: Collection<Expression>

    fun evalDouble(): Double = ((eval() ?: 0.0) as Number).toDouble()

    fun evalInt(): Int = ((eval() ?: 0) as Number).toInt()

    open fun attach(): Unit = children.forEach {
        try {
            it.attach()
        } finally {
        }
    }


    open fun detach(): Unit = children.forEach {
        try {
            it.detach()
        } finally {
        }
    }
}