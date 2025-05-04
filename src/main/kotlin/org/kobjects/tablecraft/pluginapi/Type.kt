package org.kobjects.tablecraft.pluginapi

import kotlin.enums.EnumEntries

/** This looks odd because it used to be an enum */
interface Type {
    object INT: Type {}
    object REAL: Type {}
    object BOOL: Type
    object STRING: Type
    object DATE: Type
    object VOID: Type
    object RANGE: Type;

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


    }

}