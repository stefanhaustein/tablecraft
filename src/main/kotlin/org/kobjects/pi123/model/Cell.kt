package org.kobjects.pi123.model

import org.kobjects.pi123.model.expression.Expression
import org.kobjects.pi123.model.expression.LiteralExpression
import org.kobjects.pi123.model.parser.FormulaParser
import org.kobjects.pi123.model.parser.ParsingContext
import java.util.concurrent.atomic.AtomicReference

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var rawValue: String = ""
    var expression: Expression = LiteralExpression(null)
    var computedValue_: Any? = null

    var tag = 0L
    var formulaTag = 0L

    val depenencies = mutableListOf<Cell>()
    val dependsOn = mutableListOf<Cell>()


    fun setValue(value: String, runtimeContext: RuntimeContext?) {
        rawValue = value
        expression.detachAll()
        expression = if (value.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = FormulaParser.parseExpression(value.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                LiteralExpression(e)
            }
        } else {
            try {
                LiteralExpression(value.toDouble())
            } catch (e: Exception) {
                LiteralExpression(value)
            }
        }
        if (runtimeContext != null) {
            updateAllDependencies(runtimeContext)
            Model.notifyContentUpdated(runtimeContext)
        }
    }

    fun getComputedValue(context: RuntimeContext): Any? {
        if (context.tag > tag) {
            try {
            computedValue_ = expression.eval(context)
            } catch(e: Exception) {
                e.printStackTrace()
                computedValue_ = e
            }
            tag = context.tag
        }
        return computedValue_
    }

    fun updateAllDependencies(context: RuntimeContext) {
        if (context.tag > tag) {
            getComputedValue(context)
            for (dep in depenencies) {
                dep.updateAllDependencies(context)
            }
        }
    }

}