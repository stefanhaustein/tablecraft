package org.kobjects.tablecraft.json

interface ToJson {

    fun toJson(): String = StringBuilder().also { this.toJson(it) }.toString()

    fun toJson(sb: StringBuilder)
}