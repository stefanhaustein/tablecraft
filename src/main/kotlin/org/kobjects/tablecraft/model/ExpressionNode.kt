package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.model.expression.Expression
import org.kobjects.tablecraft.model.expression.Literal
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser
import org.kobjects.tablecraft.pluginapi.ModificationToken

abstract class ExpressionNode : Dependable {

    abstract val rawFormula: String
    var validation: Map<String, Any?>? = null

    var expression: Expression = Literal(Unit)
    var value: Any = Unit

    var valueTag = 0L
    var formulaTag = 0L

    override val dependencies = mutableSetOf<ExpressionNode>()
    val dependsOn = mutableListOf<ExpressionNode>()


    open fun updateValue(tag: Long): Boolean {
        var newValue: Any
        try {
            newValue = expression.eval(EvaluationContext())
        } catch (e: Exception) {
            newValue = e
        }
        return if (newValue == value) false else {
            value = newValue
            valueTag = tag
            true
        }
    }

    fun reparse() {
        for (dep in dependencies) {
            dep.dependsOn.remove(this)
        }
        dependsOn.clear()

        expression.detachAll()
        expression = if (rawFormula.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = TcFormulaParser.parseExpression(rawFormula.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                Literal(e)
            }
        } else {
            when (rawFormula.lowercase()) {
                "true" -> Literal(true)
                "false" -> Literal(false)
                else -> {
                    try {
                        Literal(Values.parseNumber(rawFormula))
                    } catch (e: Exception) {
                        Literal(rawFormula)
                    }
                }
            }
        }
    }


}