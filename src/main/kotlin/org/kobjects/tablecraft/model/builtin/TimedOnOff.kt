package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.StatefulOperation
import java.util.Timer
import java.util.TimerTask

class TimedOnOff(
    val delayedState: Boolean,
    val delay: Double,

) : StatefulOperation {

    val timer = Timer()
    var task: TimerTask? = null
    var outputState: Boolean = false

    var host: OperationHost? = null

    override fun attach(host: OperationHost) {
        this.host = host
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
                    Model.applySynchronizedWithToken {
                        host!!.notifyValueChanged(it)
                    }
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
        fun createTon(configuration: Map<String, Any>): TimedOnOff {
            return TimedOnOff(true, (configuration["delay"] as Number).toDouble())
        }

        fun createToff(configuration: Map<String, Any>): TimedOnOff {
            return TimedOnOff(false, (configuration["delay"] as Number).toDouble())
        }
    }
}