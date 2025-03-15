package org.kobjects.tablecraft.model.builtin

import kotlinx.datetime.Clock
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.StatefulOperation
import java.util.Timer
import java.util.TimerTask

class NowFunction(
    val updateInterval: Double,
) : StatefulOperation {
    val timer = Timer()
    val timerId = timerCounter++
    var task: TimerTask? = null
    var host: OperationHost? = null

    override fun apply(params: Map<String, Any>): Any {
        return Clock.System.now()
    }


    override fun attach(host: OperationHost) {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            task = object : TimerTask() {
                override fun run() {
                    Model.applySynchronizedWithToken {
                        host.notifyValueChanged(it)
                    }
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
        host = null
        //timer.purge()
    }


    companion object {
        var timerCounter = 0

        fun create(configuration: Map<String, Any>): NowFunction {
            return NowFunction((configuration["interval"] as Number).toDouble())
        }

    }
}