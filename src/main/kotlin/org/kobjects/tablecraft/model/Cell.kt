package org.kobjects.tablecraft.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.model.expression.Expression
import org.kobjects.tablecraft.model.expression.Literal
import org.kobjects.tablecraft.model.parser.ParsingContext
import org.kobjects.tablecraft.model.parser.TcFormulaParser
import org.kobjects.tablecraft.pluginapi.ModificationToken

class Cell(
    val sheet: Sheet,
    id: String
) : Node, Iterable<Cell>, ToJson {

    val column = getColumn(id)
    val row = getRow(id)


    val id: String
        get() = id(column, row)

    var rawFormula = ""
    var image: String? = null

    var validation: Map<String, Any?>? = null

    var expression: Expression = Literal(Unit)
    override var value: Any = Unit

    override var valueTag = 0L
    var formulaTag = 0L

    override val inputs = mutableSetOf<Node>()
    override val dependencies = mutableSetOf<Node>()


    override fun updateValue(tag: Long): Boolean {
        var newValue: Any
        try {
            newValue = expression.eval(EvaluationContext())
        } catch (e: Exception) {
            e.printStackTrace()
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
        for (dep in inputs) {
            dep.dependencies.remove(this)
        }
        inputs.clear()
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
                e.printStackTrace()
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

    fun clear(modificationToken: ModificationToken) {
        setFormula("", modificationToken)
        setImage("", modificationToken)
        setValidation(emptyMap(), modificationToken)
    }

    fun setFormula(value: String, modificationToken: ModificationToken) {
        if (value != rawFormula) {
            rawFormula = value
            reparse()
            formulaTag = modificationToken.tag
            modificationToken.formulaChanged = true
            modificationToken.addRefresh(this)
        }
    }

    fun setImage(path: String, modificationToken: ModificationToken) {
        image = path
        formulaTag = modificationToken.tag
        modificationToken.formulaChanged = true
    }

    fun setJson(json: Map<String, Any?>, modificationToken: ModificationToken) {
       val formula = json["f"]
       if (formula != null) {
           setFormula(formula.toString(), modificationToken)
       }
       val validation = json["v"]
       if (validation != null) {
           setValidation(validation as Map<String, Any?>, modificationToken)
       }
        val image = json["i"]
        if (image is String) {
            setImage(image, modificationToken)
        }
    }


    fun setValidation(validation: Map<String, Any?>?, modificationToken: ModificationToken) {
        if (validation != this.validation) {
            this.validation = validation
            modificationToken.formulaChanged = true
        }
    }


    fun serializeValue(sb: StringBuilder) {
        val value = this.value
        when (value) {
            null,
                is Unit -> {sb.append("null")}
            is Boolean -> sb.append(value.toString())
            is Double -> sb.append(value.toFloat())
            is Number -> sb.append(value)
            is Exception -> sb.append("""{"type": "err", "msg": ${(value::class.simpleName.toString() + value.message).quote()}}""")
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
            sb.append("""$id = {"f": ${rawFormula.quote()}""")
            if (validation != null) {
                sb.append(""", "v":""")
                validation.toJson(sb)
            }
            if (image != null) {
                sb.append(""", "i": ${image!!.quote()}""")
            }
            if (includeComputed) {
                serializeDependencies(sb)
                sb.append(""", "c":""")
                serializeValue(sb)
            }
            sb.append("}\n")
        } else if (valueTag > tag) {
            sb.append("$id.c: ")
            serializeValue(sb)
            sb.append('\n')
        }
    }

    override fun toJson(sb: StringBuilder) {
        serialize(sb, -1, false)
    }

    override fun qualifiedId() = "${sheet.name}!$id"

    override fun iterator(): Iterator<Cell> = setOf(this).iterator()

    override fun toString() = qualifiedId() + ":" + rawFormula// rawFormula

    companion object {
        val TIME_FORMAT_MINUTES = LocalTime.Format {
            hour(); char(':'); minute(); // char(':'); second()
        }
        val TIME_FORMAT_SECONDS = LocalTime.Format {
            hour(); char(':'); minute(); char(':'); second()
        }

        fun id(column: Int, row: Int) = (column + 65).toChar().toString() + row

        fun getColumn(key: String) = key[0].code - 'A'.code


        fun getRow(key: String) = key.substring(1).toInt()

    }
}