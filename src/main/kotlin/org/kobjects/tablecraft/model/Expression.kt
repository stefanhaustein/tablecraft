package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.model.expression.Node
import org.kobjects.tablecraft.model.expression.LiteralNode
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser
import org.kobjects.tablecraft.pluginapi.ModificationToken

open class Expression : Dependable {

    var rawValue: String = ""
    var validation: Map<String, Any?>? = null

    var expression: Node = LiteralNode(Unit)
    var value: Any = Unit

    var valueTag = 0L
    var formulaTag = 0L

    override val dependencies = mutableSetOf<Expression>()
    val dependsOn = mutableListOf<Expression>()
    val changeListeners = mutableListOf<(Any)->Unit>()

    fun updateValue(tag: Long): Boolean {

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
        expression.detachAll()
        expression = if (rawValue.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = TcFormulaParser.parseExpression(rawValue.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                LiteralNode(e)
            }
        } else {
            when (rawValue.lowercase()) {
                "true" -> LiteralNode(true)
                "false" -> LiteralNode(false)
                else -> {
                    try {
                        LiteralNode(Values.parseNumber(rawValue))
                    } catch (e: Exception) {
                        LiteralNode(rawValue)
                    }
                }
            }
        }
    }


    fun setFormula(value: String, modificationToken: ModificationToken) {
        if (value != rawValue) {
            rawValue = value
            reparse()
            formulaTag = modificationToken.tag
            modificationToken.formulaChanged = true
            modificationToken.addRefresh(this)
        }
    }

}