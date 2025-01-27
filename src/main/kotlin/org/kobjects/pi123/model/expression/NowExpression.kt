package org.kobjects.pi123.model.expression

import kotlinx.datetime.Clock
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.model.parser.ParsingContext
import java.util.Timer
import java.util.TimerTask

class NowExpression(context: ParsingContext, params: Map<String, Expression>) : Expression() {

    val updateInterval = ((((params.get("0") ?: LiteralExpression(0.0)) as LiteralExpression).value) as Number).toDouble()
    var timer = Timer()
    val target = context.cell
    val timerId = timerCounter++

    override fun eval(context: RuntimeContext): Any {
        return Clock.System.now()
    }

    override val children: Collection<Expression>
        get() = emptyList()

    override fun attach() {
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            val timerTask = object : TimerTask() {
                override fun run() {
                    Model.withLock {
                        println("timer $timerId update: ${it.tag}")
                        target.updateAllDependencies(it)
                        Model.notifyContentUpdated(it)
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