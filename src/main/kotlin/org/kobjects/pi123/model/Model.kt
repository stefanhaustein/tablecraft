package org.kobjects.pi123.model

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.Plugin
import org.kobjects.pi123.plugins.pi4j.Pi4jPlugin
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
    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val lock = ReentrantLock()
    val listeners = mutableSetOf<() -> Unit>()

    val functionMap = mutableMapOf<String, FunctionSpec>()
    val plugins = mutableListOf<Plugin>()

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (function in plugin.functionSpecs) {
            functionMap[function.name] = function
        }
    }

    init {
        addPlugin(Pi4jPlugin())
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <T> withLock(action: (RuntimeContext) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return lock.withLock {
            val runtimeContext = RuntimeContext()
            action(runtimeContext)
        }
    }

    init {
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

    fun functionsToJson() = functionMap.values.joinToString(",\n", "[\n", "\n]") { it.toJson() }
}