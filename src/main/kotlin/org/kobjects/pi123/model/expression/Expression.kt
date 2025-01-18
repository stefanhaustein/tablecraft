package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.RuntimeContext

abstract class Expression {

    abstract fun eval(context: RuntimeContext): Any?

    abstract val children: Collection<Expression>

    fun evalDouble(context: RuntimeContext): Double = ((eval(context) ?: 0.0) as Number).toDouble()

    fun evalInt(context: RuntimeContext): Int = ((eval(context) ?: 0) as Number).toInt()

    open fun attach() = Unit

    open fun detach() = Unit

    fun attachAll() {
        for (child in children) {
            child.attachAll()
        }
        try {
            attach()
        } catch(e: Exception ) {
            e.printStackTrace()
        }
    }


    fun detachAll(){
        for (child in children) {
            child.detachAll()
        }
        try {
            detach()
        } catch(e: Exception ) {
        e.printStackTrace()
    }
    }
}