package org.kobjects.pi123.model

import com.pi4j.Pi4J
import com.pi4j.context.Context
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import org.json.JSONObject

object Model {
    val pi4J: Context
    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))

    init {
        try {
            val json = JSONObject(File("storage/model.json").readText())
            for (name in json.keys()) {
                val sheet = Sheet(name)
                sheets[name] = sheet
                sheet.parseJson(json.getJSONObject(name))
            }

            println("json: $json")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        pi4J = Pi4J.newAutoContext()
        println("pi4j: $pi4J")
    }
    


    fun set(name: String, value: String) {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        sheet.set(name.substring(cut + 1), value)
    }

    fun save() {
        File("storage").mkdir()
        val writer = FileWriter("storage/model.json")
        writer.write("{\n")
        var first = true
        for (sheet in sheets.values) {
            if (first) {
                first = false
            } else {
                writer.write(",\n")
            }
            writer.write("${sheet.name.quote()}: ${sheet.serializeValues(Sheet.ValueType.FORMULA)}")
        }
        writer.write("\n}\n")
        writer.close()
    }

}