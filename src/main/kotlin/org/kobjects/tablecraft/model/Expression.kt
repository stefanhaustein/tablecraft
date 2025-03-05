package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.Node
import org.kobjects.tablecraft.model.expression.LiteralNode
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser

open class Expression {

    var rawValue: String = ""
    var validation: Map<String, Any?>? = null

    var expression: Node = LiteralNode(Unit)
    var computedValue_: Any = Unit

    var tag = 0L
    var formulaTag = 0L

    val depenencies = mutableListOf<Expression>()
    val dependsOn = mutableListOf<Expression>()
    val changeListeners = mutableListOf<()->Unit>()

    fun getComputedValue(context: RuntimeContext): Any {
        if (context.tag > tag) {
            try {
                val newValue = expression.eval(context)
                if (newValue != computedValue_) {
                    computedValue_ = newValue
                    for (listener in changeListeners) {
                        listener.invoke()
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                computedValue_ = e
            }
            tag = context.tag
        }
        return computedValue_
    }

    fun setValue(value: String, runtimeContext: RuntimeContext?) {
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
        if (runtimeContext != null) {
            updateAllDependencies(runtimeContext)
            Model.notifyContentUpdated(runtimeContext)
        }
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