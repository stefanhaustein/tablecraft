package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.ValueChangeListener
import org.kobjects.tablecraft.pluginapi.StatefulFunctionInstance
import java.util.Timer
import java.util.TimerTask

class TimedPulse(
    val delay: Double,
) : StatefulFunctionInstance {

    val timer = Timer()
    var task: TimerTask? = null
    var outputState: Boolean = false
    var armed: Boolean = true

    var host: ValueChangeListener? = null

    override fun attach(host: ValueChangeListener) {
        this.host = host
    }

    override fun apply(context: EvaluationContext, params: Map<String, Any>): Any {
        val inputState = params["input"] as Boolean
        if (inputState) {
            if (!armed) {
                armed = true
            } else {
                armed = false
                if (!outputState) {
                    outputState = true
                    task = object : TimerTask() {
                        override fun run() {
                            outputState = false
                            Model.applySynchronizedWithToken {
                                host!!.notifyValueChanged(it)
                            }
                            task = null
                        }
                    }
                    timer.schedule(task, (delay * 1000.0).toLong())
                }
            }
        }
        return outputState
    }


    override fun detach() {
        task?.cancel()
        task = null
    }


    companion object {
        fun create(configuration: Map<String, Any>): TimedPulse {
            return TimedPulse((configuration["delay"] as Number).toDouble())
        }
    }
}