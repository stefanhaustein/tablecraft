package org.kobjects.pi123.model

fun String.quote(): String {
    val sb = StringBuilder()
    sb.append('"')
    for (i in this.indices) {
        when (val c = this[i]) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> sb.append(c)
        }
    }
    sb.append('"')
    return sb.toString()
}