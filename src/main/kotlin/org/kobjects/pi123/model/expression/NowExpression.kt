package org.kobjects.pi123.model.expression

import org.kobjects.pi123.model.RuntimeContext
import org.kobjects.pi123.model.parser.ParsingContext
import java.util.Timer
import java.util.TimerTask

class NowExpression(context: ParsingContext, params: Map<String, Expression>) : Expression() {

    val updateInterval = ((((params.get("0") ?: LiteralExpression(0.0)) as LiteralExpression).value) as Number).toDouble()
    var timer = Timer()
    val target = context.cell

    override fun eval(context: RuntimeContext): Any {
        return System.currentTimeMillis().toDouble() / 86400000.0
    }

    override val children: Collection<Expression>
        get() = emptyList()

    override fun attach() {
        super.attach()
        val period = (updateInterval * 1000.0).toLong()
        if (period > 0) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val context = RuntimeContext()
                    println("timer based update: ${context.tag}")
                    target.updateAllDependencies(context)
                }
            }, period, period)
    }
    }

    override fun detach() {
        super.detach()
        timer.cancel()
    }
}