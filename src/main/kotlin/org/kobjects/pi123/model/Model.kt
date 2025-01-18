package org.kobjects.pi123.model

import com.pi4j.Pi4J
import com.pi4j.context.Context
import java.io.File
import java.io.FileWriter
import org.kobjects.pi123.toml.TomlParser
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object Model {
    var modificationTag: Long = 0
    lateinit var pi4J: Context
    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val lock = ReentrantLock()
    val listeners = mutableSetOf<() -> Unit>()

    @OptIn(ExperimentalContracts::class)
    inline fun <T> withLock(action: (RuntimeContext) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return lock.withLock {
            val runtimeContext = RuntimeContext()
            action(runtimeContext)
        }
    }

    init {
        try {
            pi4J = Pi4J.newAutoContext()
            println("pi4j: $pi4J")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        withLock { runtimeContext ->
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

            for (sheet in sheets.values) {
                sheet.updateAll(runtimeContext)
            }
        }
    }


    fun set(name: String, value: String, runtimeContext: RuntimeContext?) {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        sheet.set(name.substring(cut + 1), value, runtimeContext)
    }

    fun save() {
        File("storage").mkdir()
        val writer = FileWriter("storage/model.ini")
        for (sheet in sheets.values) {
            writer.write("[sheets.${sheet.name}.cells]\n\n")
            writer.write(sheet.serialize(-1, false))
            writer.write("\n")
        }
        writer.close()
    }


    fun notifyContentUpdated(runtimeContext: RuntimeContext) {
        modificationTag = runtimeContext.tag
        for (listener in listeners) {listener()}
        listeners.clear()
    }
}