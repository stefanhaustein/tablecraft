package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.model.parser.ParsingContext
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.withLock

class NowExpression(context: ParsingContext, params: Map<String, Expression>) : Expression() {

    val updateInterval = ((((params.get("0") ?: LiteralExpression(0.0)) as LiteralExpression).value) as Number).toDouble()
    var timer = Timer()
    val target = context.cell
    val timerId = timerCounter++

    override fun eval(context: RuntimeContext): Any {
        return System.currentTimeMillis().toDouble() / 86400000.0
    }

    override val children: Collection<Expression>
        get() = emptyList()

    override fun attach() {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            val timerTask = object : TimerTask() {
                override fun run() {
                    Model.lock.withLock {
                        val context = RuntimeContext()
                        println("timer $timerId update: ${context.tag}")
                        target.updateAllDependencies(context)
                        Model.notifyContentUpdated(context)
                    }
                }
            }
            timer.schedule(timerTask, period, period)
        }
        println("Started timer $timerId")
    }

    override fun detach() {
        println("Cancelling timer $timerId!")
        //timerTask?.cancel()
        timer.cancel()
        //timer.purge()
    }
    companion object {
        var timerCounter = 0
    }
}