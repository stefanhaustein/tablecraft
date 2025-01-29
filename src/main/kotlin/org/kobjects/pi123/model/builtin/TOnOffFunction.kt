package org.kobjects.pi123.model.builtin

import org.kobjects.pi123.pluginapi.FunctionInstance
import java.util.Timer
import java.util.TimerTask

class TOnOffFunction(
    val delayedState: Boolean,
    val delay: Double,
    val listener: (Any) -> Unit
) : FunctionInstance {

    val timer = Timer()
    var task: TimerTask? = null
    var outputState: Boolean = false

    override fun attach() {
    }

    override fun apply(params: Map<String, Any>): Any {
        val inputState = params["input"] as Boolean
        if (inputState != delayedState) {
            task?.cancel()
            outputState = inputState
        } else if (outputState != delayedState) {
            task?.cancel()
            task = object : TimerTask() {
                override fun run() {
                    outputState = delayedState
                    listener(delayedState)
                    task = null
                }
            }
            timer.schedule(task, (delay * 1000.0).toLong())
        }
        return outputState
    }


    override fun detach() {
        task?.cancel()
        task = null
    }


    companion object {
        fun createTon(params: Map<String, Any>, listener: ((Any) -> Unit)): TOnOffFunction {
            return TOnOffFunction(true, (params["delay"] as Number).toDouble(), listener)
        }

        fun createToff(params: Map<String, Any>, listener: ((Any) -> Unit)): TOnOffFunction {
            return TOnOffFunction(false, (params["delay"] as Number).toDouble(), listener)
        }
    }
}