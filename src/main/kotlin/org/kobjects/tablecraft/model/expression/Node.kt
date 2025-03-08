package org.kobjects.tablecraft.model.expression

import kotlinx.datetime.*
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.model.Values

abstract class Node {

    abstract fun eval(context: ModificationToken): Any

    abstract val children: Collection<Node>

    fun evalDouble(context: ModificationToken): Double = Values.toDouble(eval(context))

    fun evalInt(context: ModificationToken): Int = Values.toInt(eval(context))

    fun evalBoolean(context: ModificationToken): Boolean = Values.toBoolean(eval(context))

    open fun attach() = Unit

    open fun detach() = Unit

    fun attachAll() {
        for (child in children) {
            child.attachAll()
        }
        try {
            attach()
        } catch(e: Exception ) {
            System.err.println("Error attaching $this")
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


    }
}