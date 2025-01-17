package org.kobjects.pi123.model

import com.pi4j.Pi4J
import com.pi4j.context.Context
import java.io.File
import java.io.FileWriter
import org.kobjects.pi123.toml.TomlParser

object Model {
    val pi4J: Context
    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))

    init {
        try {
            val toml = TomlParser.parse(File("storage/model.ini").readText())
            for ((key, map) in toml) {
                if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                    val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                    val sheet = Sheet(name)
                    sheets[name] = sheet
                    sheet.parseToml(map)
                }
            }

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
        val writer = FileWriter("storage/model.ini")
        for (sheet in sheets.values) {
            writer.write("[sheets.${sheet.name}.cells]\n\n")
            writer.write(sheet.serializeValues(Sheet.ValueType.FORMULA, true))
            writer.write("\n")
        }
        writer.close()
    }

}