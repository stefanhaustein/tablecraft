package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.model.expression.Expression
import org.kobjects.tablecraft.model.expression.Literal
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser

abstract class ExpressionNode : Node {

    abstract val rawFormula: String
    var validation: Map<String, Any?>? = null

    var expression: Expression = Literal(Unit)
    override var value: Any = Unit

    override var valueTag = 0L
    var formulaTag = 0L

    override val dependencies = mutableSetOf<Node>()
    override val dependsOn = mutableSetOf<Node>()


    override fun updateValue(tag: Long): Boolean {
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

    override fun detach() {
        clearDependsOn()
    }

    fun clearDependsOn() {
        for (dep in dependsOn) {
            dep.dependencies.remove(this)
        }
        dependsOn.clear()
    }

    fun reparse() {
        clearDependsOn()
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