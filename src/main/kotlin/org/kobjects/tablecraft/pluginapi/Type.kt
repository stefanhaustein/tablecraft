package org.kobjects.tablecraft.pluginapi

enum class Type {
    INT, REAL, BOOL, STRING, DATE, VOID, RANGE;

    fun fromString(s: String): Any = when(this) {
        INT -> s.toInt()
        REAL -> s.toDouble()
        BOOL -> s.toBoolean()
        STRING -> s
        else -> throw UnsupportedOperationException("Can't parse $this yet.")
    }

    fun fromJson(value: Any): Any = if (value is String) fromString(value) else when(this) {
        INT -> (value as Number).toInt()
        REAL -> (value as Number).toDouble()
        BOOL -> value as Boolean
        STRING -> value.toString()
        else -> throw UnsupportedOperationException("Can't parse $this from JSON yet.")
    }

}