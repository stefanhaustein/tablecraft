package org.kobjects.tablecraft.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.expression.PortReference
import org.kobjects.tablecraft.pluginapi.ModificationToken

class Cell(
    val sheet: Sheet,
    id: String
) : ExpressionNode(), Iterable<Cell>, ToJson {

    val column: Int
    val row: Int

    init {
        column = id[0].code - 'A'.code
        row = id.substring(1).toInt()
    }

    val id: String
        get() = id(column, row)

    override var rawFormula = ""
    var image: String? = null


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
    }
}