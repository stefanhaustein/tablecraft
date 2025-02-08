package org.kobjects.pi123.model.expression

import kotlinx.datetime.*
import org.kobjects.pi123.model.RuntimeContext
import kotlin.time.DurationUnit

abstract class Expression {

    abstract fun eval(context: RuntimeContext): Any

    abstract val children: Collection<Expression>

    fun evalDouble(context: RuntimeContext): Double = toDouble(eval(context))

    fun evalInt(context: RuntimeContext): Int = toInt(eval(context))

    fun evalBoolean(context: RuntimeContext): Boolean = toBoolean(eval(context))

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

    companion object {
        val ZERO_TIME = LocalDateTime(1900, 1, 1, 0, 0)

        fun toDouble(value: Any?) = when (value) {
            null, Unit -> 0.0
            is Double -> value
            is Number -> value.toDouble()
            is Instant -> (value - ZERO_TIME.toInstant(TimeZone.currentSystemDefault())).toDouble(DurationUnit.DAYS)
            else -> throw IllegalArgumentException("Not a number: ${value::class.qualifiedName}: '$value'")
        }

        fun toInt(value: Any?) = when (value) {
            null, Unit -> 0
            is Int -> value.toInt()
            is Number -> value.toInt()
            else -> throw IllegalArgumentException("Not convertible to int: ${value::class.qualifiedName}: '$value'")
        }

        fun toBoolean(value: Any?) = when (value) {
            null, Unit -> false
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            else -> throw IllegalArgumentException("Not convertible to boolean: ${value::class.qualifiedName}: '$value'")
        }

    }
}