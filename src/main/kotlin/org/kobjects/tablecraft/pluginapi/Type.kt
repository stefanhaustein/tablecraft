package org.kobjects.tablecraft.pluginapi

enum class Type {
    INT, NUMBER, BOOLEAN, TEXT, DATE, IMAGE;

    fun fromString(s: String): Any = when(this) {
        INT -> s.toInt()
        NUMBER -> s.toDouble()
        BOOLEAN -> s.toBoolean()
        TEXT -> s.toString()
        else -> throw UnsupportedOperationException("Can't parse $this yet.")
    }


}