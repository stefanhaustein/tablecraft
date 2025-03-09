package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.Node
import org.kobjects.tablecraft.model.expression.LiteralNode
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser
import org.kobjects.tablecraft.pluginapi.ModificationToken

open class Expression {

    var rawValue: String = ""
    var validation: Map<String, Any?>? = null

    var expression: Node = LiteralNode(Unit)
    var computedValue_: Any = Unit

    var valueTag = 0L
    var formulaTag = 0L

    val depenencies = mutableListOf<Expression>()
    val dependsOn = mutableListOf<Expression>()
    val changeListeners = mutableListOf<(Any)->Unit>()

    fun getComputedValue(context: ModificationToken): Any {
        if (context.tag > valueTag) {
            try {
                val newValue = expression.eval(context)
                if (newValue != computedValue_) {
                    computedValue_ = newValue
                    for (listener in changeListeners) {
                        listener.invoke(newValue)
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                computedValue_ = e
            }
            valueTag = context.tag
        }
        return computedValue_
    }

    fun reparse(token: ModificationToken) {
        setFormula(rawValue, token)
    }


    fun setFormula(value: String, modificationToken: ModificationToken) {
        // No cache check; setFormula is currently used for re-parsing (should be moved to a "reset" function)
        rawValue = value
        expression.detachAll()
        expression = if (value.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = TcFormulaParser.parseExpression(value.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                LiteralNode(e)
            }
        } else {
            when (value.lowercase()) {
                "true" -> LiteralNode(true)
                "false" -> LiteralNode(false)
                else -> {
                    try {
                        LiteralNode(Values.parseNumber(value))
                    } catch (e: Exception) {
                        LiteralNode(value)
                    }
                }
            }
        }

        formulaTag = modificationToken.tag

        updateAllDependencies(modificationToken)
        Model.notifyContentUpdated(modificationToken)
    }


    fun updateAllDependencies(context: ModificationToken) {
        if (context.tag > valueTag) {
            getComputedValue(context)
            for (dep in depenencies) {
                dep.updateAllDependencies(context)
            }
        }
    }

}