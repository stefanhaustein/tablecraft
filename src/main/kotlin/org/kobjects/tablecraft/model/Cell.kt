package org.kobjects.tablecraft.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.builtin.ImageReference
import org.kobjects.tablecraft.model.expression.Expression
import org.kobjects.tablecraft.model.expression.LiteralExpression
import org.kobjects.tablecraft.model.parser.TcFormulaParser
import org.kobjects.tablecraft.model.parser.ParsingContext

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var rawValue: String = ""
    var validation: Map<String, Any?>? = null

    var expression: Expression = LiteralExpression(Unit)
    var computedValue_: Any = Unit

    var tag = 0L
    var formulaTag = 0L

    val depenencies = mutableListOf<Cell>()
    val dependsOn = mutableListOf<Cell>()

    fun setJson(json: Map<String, Any?>, runtimeContext: RuntimeContext?) {
       val formula = json["f"]
       if (formula != null) {
           setValue(formula.toString(), runtimeContext)
       }
       val validation = json["v"]
       if (validation != null) {
           setValidation(validation as Map<String, Any?>, runtimeContext)
       }
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
                LiteralExpression(e)
            }
        } else {
            when (value.lowercase()) {
                "true" -> LiteralExpression(true)
                "false" -> LiteralExpression(false)
                else -> {
                    try {
                        LiteralExpression(Values.parseNumber(value))
                    } catch (e: Exception) {
                        LiteralExpression(value)
                    }
                }
            }
        }
        if (runtimeContext != null) {
            updateAllDependencies(runtimeContext)
            Model.notifyContentUpdated(runtimeContext)
        }
    }

    fun setValidation(validation: Map<String, Any?>?, runtimeContext: RuntimeContext?) {
        this.validation = validation
        if (runtimeContext != null) {
            Model.notifyContentUpdated(runtimeContext)
        }
    }

    fun getComputedValue(context: RuntimeContext): Any {
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

    fun serializeValue(sb: StringBuilder) {
        val value = computedValue_
        when (value) {
            null,
                is Unit -> {sb.append("null")}
            is Boolean -> sb.append(value.toString())
            is Double -> sb.append(value.toFloat())
            is Number -> sb.append(value)
            is Exception -> sb.append("""{"type": "err", "msg": ${(value::class.simpleName.toString() + value.message).quote()}}""")
            is ImageReference -> sb.append("""{"type": "img", "src":${value.source.quote()}}""")
            is Instant -> {
                val localDateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
                /* sb.append(localDateTime.date.format(LocalDate.Formats.ISO))
                 sb.append(' ') */
                sb.append("""{"type": "instant", "rendered":${localDateTime.time.format(TIME_FORMAT_SECONDS).quote()}}""")
            }
            else -> sb.append(value.toString().quote())
        }
    }

    fun serialize(sb: StringBuilder, tag: Long, includeComputed: Boolean) {
        val id = id
        if (formulaTag > tag) {
            sb.append("""$id = {"f": ${rawValue.quote()}""")
            if (validation != null) {
                sb.append(""", "v":""")
                validation.toJson(sb)
            }
            sb.append("}\n")
        }
        if (includeComputed && this.tag > tag) {
            sb.append("$id.c = ")
            serializeValue(sb)
            sb.append('\n')
        }
    }

    override fun toString() = rawValue

    companion object {
        val TIME_FORMAT_MINUTES = LocalTime.Format {
            hour(); char(':'); minute(); // char(':'); second()
        }
        val TIME_FORMAT_SECONDS = LocalTime.Format {
            hour(); char(':'); minute(); char(':'); second()
        }
    }

}