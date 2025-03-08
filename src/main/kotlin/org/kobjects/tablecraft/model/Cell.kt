package org.kobjects.tablecraft.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.builtin.ImageReference
import org.kobjects.tablecraft.pluginapi.ModificationToken

class Cell(
    val sheet: Sheet,
    val id: String
) : Expression() {


    fun setJson(json: Map<String, Any?>, modificationToken: ModificationToken?) {
       val formula = json["f"]
       if (formula != null) {
           setFormula(formula.toString(), modificationToken)
       }
       val validation = json["v"]
       if (validation != null) {
           setValidation(validation as Map<String, Any?>, modificationToken)
       }
    }


    fun setValidation(validation: Map<String, Any?>?, modificationToken: ModificationToken?) {
        this.validation = validation
        if (modificationToken != null) {
            Model.notifyContentUpdated(modificationToken)
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
        if (includeComputed && this.valueTag > tag) {
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