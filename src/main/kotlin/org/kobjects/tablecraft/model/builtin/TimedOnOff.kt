package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance
import java.util.Timer
import java.util.TimerTask

class TimedOnOff(
    val delayedState: Boolean,
    val delay: Double,
    val host: OperationHost,
) : OperationInstance {

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
                    Model.applySynchronizedWithToken {
                        host.notifyValueChanged(delayedState, it)
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
        fun createTon(host: OperationHost): TimedOnOff {
            return TimedOnOff(true, (host.configuration["delay"] as Number).toDouble(), host)
        }

        fun createToff(host: OperationHost): TimedOnOff {
            return TimedOnOff(false, (host.configuration["delay"] as Number).toDouble(), host)
        }
    }
}