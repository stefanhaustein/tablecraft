package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.ValueChangeListener
import org.kobjects.tablecraft.pluginapi.StatefulFunctionInstance

class FlipflopFunction(
    val rs: Boolean,
) : StatefulFunctionInstance {

    var q: Boolean = false

    override fun attach(host: ValueChangeListener) {}

    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any? {
        val s = params["s"] as Boolean
        val r = params["r"] as Boolean

        if (s) {
            if (r) {
                q = rs
            } else {
                q = true
            }
        } else if (r) {
            q = false
        }
        return q
    }


    override fun detach() {
    }


    companion object {
        fun createRs(): FlipflopFunction {
            return FlipflopFunction(true)
        }

        fun createSr(): FlipflopFunction {
            return FlipflopFunction(false)
        }
    }
}