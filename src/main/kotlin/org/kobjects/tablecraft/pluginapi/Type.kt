package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import kotlin.enums.EnumEntries

/** This looks odd because it used to be an enum */
interface Type : ToJson {
    object INT: Type {
        override fun toString() = "Int"
    }
    object REAL: Type {
        override fun toString() = "Real"
    }
    object BOOL: Type {
        override fun toString() = "Bool"
    }
    object STRING: Type {
        override fun toString() = "String"
    }
    object DATE: Type {
        override fun toString() = "Date"
    }
    object VOID: Type {
        override fun toString() = "Void"
    }
    object RANGE: Type {
        override fun toString() = "Range"
    }

    override fun toJson(sb: StringBuilder) {
        sb.append(toString().quote())
    }

    fun valueFromString(s: String): Any = when(this) {
        INT -> s.toInt()
        REAL -> s.toDouble()
        BOOL -> s.toBoolean()
        STRING -> s
        else -> throw UnsupportedOperationException("Can't parse $this yet.")
    }

    fun valueFromJson(value: Any): Any = if (value is String) valueFromString(value) else when(this) {
        INT -> (value as Number).toInt()
        REAL -> (value as Number).toDouble()
        BOOL -> value as Boolean
        STRING -> value.toString()
        else -> throw UnsupportedOperationException("Can't parse $this from JSON yet.")
    }

    class ENUM<T : Enum<T>>(val entries: EnumEntries<T>) : Type {

        override fun toJson(sb: StringBuilder) {
            entries.map {it.name}.toJson(sb)
        }

        override fun valueFromString(s: String) =
            entries.first { it.name.lowercase() == s.lowercase() }

    }

}