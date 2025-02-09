package org.kobjects.tablecraft.model.builtin

import kotlinx.datetime.Clock
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance
import java.util.Timer
import java.util.TimerTask

class NowFunction(
    val updateInterval: Double,
    val host: OperationHost,
) : OperationInstance {

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
                    host.notifyValueChanged(Clock.System.now())
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

        fun create(host: OperationHost): NowFunction {
            return NowFunction((host.configuration["interval"] as Number).toDouble(), host)
        }

    }
}