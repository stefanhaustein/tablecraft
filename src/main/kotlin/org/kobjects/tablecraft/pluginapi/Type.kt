package org.kobjects.tablecraft.pluginapi

enum class Type {
    INT, NUMBER, BOOLEAN, TEXT, DATE, IMAGE, VOID, RANGE;

    fun fromString(s: String): Any = when(this) {
        INT -> s.toInt()
        NUMBER -> s.toDouble()
        BOOLEAN -> s.toBoolean()
        TEXT -> s
        else -> throw UnsupportedOperationException("Can't parse $this yet.")
    }

    fun fromJson(value: Any): Any = when(this) {
        INT -> (value as Number).toInt()
        NUMBER -> (value as Number).toDouble()
        BOOLEAN -> value as Boolean
        TEXT -> value.toString()
        else -> throw UnsupportedOperationException("Can't parse $this from JSON yet.")
    }

}