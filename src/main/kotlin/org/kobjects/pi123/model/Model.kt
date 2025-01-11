package org.kobjects.pi123.model

import com.pi4j.Pi4J

object Model {
    val pi4J = Pi4J.newAutoContext()

   val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))

    fun set(name: String, value: String) {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        sheet.set(name.substring(cut + 1), value)
    }


}