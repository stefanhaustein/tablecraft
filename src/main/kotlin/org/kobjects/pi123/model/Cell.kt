package org.kobjects.pi123.model

import org.kobjects.pi123.model.expression.Expression
import org.kobjects.pi123.model.expression.LiteralExpression
import org.kobjects.pi123.model.parser.FormulaParser
import org.kobjects.pi123.model.parser.ParsingContext

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var rawValue: String = ""
    var expression: Expression = LiteralExpression(0.0)
    var computedValue_: Any = 0.0

    var tag = 0L

    val depenencies = mutableListOf<Cell>()
    val dependsOn = mutableListOf<Cell>()


    fun setValue(value: String) {
        expression.detach()
        rawValue = value
        expression = if (value.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = FormulaParser.parseExpression(value.substring(1), context)
                parsed.attach()
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
    }

    fun getComputedValue(context: RuntimeContext): Any {
        if (context.tag > tag) {
            computedValue_ = expression.eval(context)
            tag = context.tag
        }
        return computedValue_
    }


}