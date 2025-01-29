package org.kobjects.pi123.model.builtin

import kotlinx.datetime.Clock
import org.kobjects.pi123.pluginapi.FunctionInstance
import java.util.Timer
import java.util.TimerTask

class NowFunction(
    val updateInterval: Double,
    val listener: (Any) -> Unit
) : FunctionInstance {

    val timer = Timer()
    val timerId = timerCounter++
    var task: TimerTask? = null

    override fun apply(params: Map<String, Any>): Any {
        return Clock.System.now()
    }


    override fun attach() {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            task = object : TimerTask() {
                override fun run() {
                    listener(Clock.System.now())
                }
            }
            timer.schedule(task, period, period)
        }
        println("Started timer $timerId")
    }


    override fun detach() {
        println("Cancelling timer $timerId!")
        //timerTask?.cancel()
        task?.cancel()
        task = null
        //timer.purge()
    }


    companion object {
        var timerCounter = 0

        fun create(params: Map<String, Any>, listener: ((Any) -> Unit)): NowFunction {
            return NowFunction((params["interval"] as Number).toDouble(), listener)
        }

    }
}