package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.RuntimeContext

abstract class Expression {

    abstract fun eval(context: RuntimeContext): Any

    abstract val children: Collection<Expression>

    fun evalDouble(context: RuntimeContext): Double = (eval(context) as Number).toDouble()

    fun evalInt(context: RuntimeContext): Int = (eval(context) as Number).toInt()

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