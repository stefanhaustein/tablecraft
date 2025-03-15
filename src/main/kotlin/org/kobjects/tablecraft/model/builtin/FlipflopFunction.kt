package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance
import java.util.Timer
import java.util.TimerTask

class FlipflopFunction(
    val rs: Boolean,
) : OperationInstance {

    var q: Boolean = false

    override fun attach(host: OperationHost) {}

    override fun apply(params: Map<String, Any>): Any {
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