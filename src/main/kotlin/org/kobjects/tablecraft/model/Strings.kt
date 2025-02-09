package org.kobjects.tablecraft.model

fun String.escape(): String {
    val sb = StringBuilder()
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
    return sb.toString()
}


fun String.quote() = """"${escape()}""""
