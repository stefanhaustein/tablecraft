package org.kobjects.pi123.model

import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent
import com.pi4j.io.gpio.digital.PullResistance
import org.kobjects.parsek.tokenizer.RegularExpressions
import org.kobjects.pi123.model.expression.Expression
import org.kobjects.pi123.model.expression.LiteralExpression
import org.kobjects.pi123.model.parser.FormulaParser
import java.time.LocalDateTime

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var rawValue: String = ""
    var expression: Expression? = null
    var computedValue: Any? = null



    fun setValue(value: String) {
        expression?.detach()
        rawValue = value
        expression = if (value.startsWith("=")) {
            try {
                val parsed = FormulaParser.parseExpression(value.substring(1), sheet)
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


    fun updateValue() {
        computedValue = expression?.eval()
    }


}