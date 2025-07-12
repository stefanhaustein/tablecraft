package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.JsonParser
import org.kobjects.tablecraft.model.builtin.BuiltinFunctions
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.pi4j.Pi4jPlugin
import org.kobjects.tablecraft.svg.SvgManager
import java.io.File
import java.io.FileWriter
import org.kobjects.tablecraft.tomson.TomsonParser
import java.io.Writer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

object Model : ModelInterface {

    val STORAGE_FILE = File("storage/data.tc")

    var modificationTag: Long = 0

    var simulationMode_: Boolean = false
    var runMode_: Boolean = false
    var settingsTag: Long = 0

    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val listeners = mutableSetOf<() -> Unit>()

    val plugins = mutableListOf<Plugin>()

    val functions = Functions()
    val factories = Factories()
    val ports = Ports()
    val integrations = Integrations()

    val svgs = SvgManager(File("src/main/resources/static/img"))

    val restValues = mutableMapOf<String, Any>()

    private val lock = ReentrantLock()

    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin(this))
        addPlugin(svgs)
        // addPlugin(MqttPlugin)

        applySynchronizedWithToken { runtimeContext ->
            runtimeContext.loading = true
            loadData(STORAGE_FILE.readText(), runtimeContext)
        }
    }

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (spec in plugin.operationSpecs) {
            when (spec) {
                is FunctionSpec -> functions.add(spec)
                is AbstractFactorySpec -> factories.add(spec)
                else -> throw IllegalArgumentException("Function or factory expected; got $spec (${spec::class.simpleName})")
            }
        }
    }

    fun setSimulationMode(value: Boolean, token: ModificationToken) {
        simulationMode_ = value
        for (port in ports.filterIsInstance<InputPortHolder>()) {
            port.notifyValueChanged(token)
        }
        settingsTag = token.tag
    }

    fun setRunMode(value: Boolean, token: ModificationToken) {
        runMode_ = value
        settingsTag = token.tag
    }



    fun loadData(data: String, token: ModificationToken) {
        try {
            val toml = TomsonParser.parse(data)
            for ((key, map) in toml) {
                if (key.isEmpty()) {
                    setSimulationMode(map["simulationMode"] as Boolean? ?: false, token)
                    setRunMode(map["runMode"] as Boolean? ?: false, token)
                } else if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                    val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                    val sheet = Sheet(name)
                    sheets[name] = sheet
                    sheet.parseToml(map, token)
                } else if (key == "ports") {
                    for ((name, value) in map) {
                        try {
                            ports.definePort(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if (key == "integrations") {
                    for ((name, value) in map) {
                        try {
                            integrations.configureIntegration(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            System.err.println("Failed to load integration '$name'.")
                            e.printStackTrace()
                        }
                    }
                } else if (key == "simulationValues") {
                    for((key, value) in map) {
                        val port = ports[key]
                        if (port is InputPortHolder) {
                            port.notifyValueChanged(token)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }



    fun getOrCreate(name: String): Cell {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        return sheet.getOrCreateCell(name.substring(cut + 1))
    }

    fun serialize(writer: Writer, forClient: Boolean = false, tag: Long = -1) {
        if (settingsTag > tag) {
            writer.write("simulationMode = $simulationMode_\n")
            writer.write("runMode = $runMode_\n")
        }
        integrations.serialize(writer, forClient, tag)

        if (forClient) {
            writer.write(factories.serialize(tag))
            writer.write(functions.serialize(tag))
        }

        ports.serialize(writer, forClient, tag)

        writer.write("\n")

        for (sheet in sheets.values) {
            writer.write(sheet.serialize(tag, forClient))
            writer.write("\n")
        }
    }


    fun save() {
        STORAGE_FILE.mkdirs()
        val writer = FileWriter(STORAGE_FILE)
        serialize(writer)
        writer.close()
    }





    fun updateSheet(name: String?, jsonSpec: Map<String, Any>, token: ModificationToken) {
        val previousName = jsonSpec["previousName"] as? String?

        if (!name.isNullOrBlank()) {
            if (name != previousName) {
                val newSheet = Sheet(name, token.tag)
                sheets[name] = newSheet
                token.symbolsChanged = true
                if (!previousName.isNullOrBlank()) {
                    val oldSheet = sheets[previousName]
                    if (oldSheet != null) {
                        for (oldCell in oldSheet.cells.values) {
                            val newCell = newSheet.getOrCreateCell(oldCell.id)
                            newCell.setJson(JsonParser.parseObject(oldCell.toJson()), token)
                        }
                    }
                    sheets[previousName]?.delete(token)
                }
            }

        } else if (!previousName.isNullOrBlank()) {
            sheets[previousName]?.delete(token)
        }
    }


    fun clearAll(modificationToken: ModificationToken) {
        modificationToken.symbolsChanged = true
        modificationToken.formulaChanged = true

        for (key in ports.keys.toList()) {
            ports.deletePort(key, modificationToken)
        }

        for (sheet in sheets.values) {
            sheet.clear(modificationToken)
        }
    }

    fun setSimulationValue(name: String, value: Any, token: ModificationToken) {
        if (simulationMode_) {
            val port = ports[name]
            if (port is InputPortHolder) {
                port.setSimulationValue(value, token)
            }
        }
    }


    @OptIn(ExperimentalContracts::class)
    override fun <T> applySynchronizedWithToken(action: (ModificationToken) -> T): T {
        return lock.withLock {
            val modificationToken = ModificationToken()

            val result = action(modificationToken)

            if (modificationToken.symbolsChanged) {
                for (port in ports.filterIsInstance<OutputPortHolder>()) {
                    port.reparse()
                }
                for (sheet in sheets.values) {
                    for (cell in sheet.cells.values) {
                        cell.reparse()
                    }
                }
            }

            var anyChanged = modificationToken.symbolsChanged || modificationToken.formulaChanged
            if (anyChanged) {
                // Other changes are not relevant for saving.
                save()
            }

            if (modificationToken.symbolsChanged) {
                // Mark everything "dirty"
                for (sheet in sheets.values) {
                    for (cell in sheet.cells.values) {
                        modificationToken.addRefresh(cell)
                    }
                }
                for (port in ports.filterIsInstance<OutputPortHolder>()) {
                    modificationToken.addRefresh(port)
                }
            }

            println("Root nodes needing refresh: ${modificationToken.refreshRoots}; other: ${modificationToken.refreshNodes}")

            while (modificationToken.refreshRoots.isNotEmpty()) {
                val current = modificationToken.refreshRoots.first()
                modificationToken.refreshRoots.remove(current)
                // println("Updating: $current")
                if (current.updateValue(modificationToken)) {
                    anyChanged = true
                    // println("adding new dependencies: ${current.dependencies}")
                    for (dep in current.outputs) {
                        if (dep.inputs.size == 1) {
                            modificationToken.refreshNodes.remove(dep)
                            modificationToken.refreshRoots.add(dep)
                        } else {
                            modificationToken.refreshNodes.add(dep)
                        }
                    }
                }
            }

            // modificationToken.refreshNodes.addAll(modificationToken.refreshRoots)
            // modificationToken.refreshRoots.clear()

            for (dep in modificationToken.refreshNodes.toList()) {
                modificationToken.addAllDependencies(dep)
            }

            println("Saturated dependencies: ${modificationToken.refreshNodes}")

            while (modificationToken.refreshNodes.isNotEmpty()) {
                for (node in modificationToken.refreshNodes) {
                    var found = true
                    for (dep in node.inputs) {
                        if (modificationToken.refreshNodes.contains(dep)) {
                            found = false
                            break
                        }
                    }
                    if (found) {
                        println("Updating node: $node")
                        modificationToken.refreshNodes.remove(node)
                        if (node.updateValue(modificationToken)) {
                            anyChanged = true
                        }
                        break
                    }
                }
            }

            modificationTag = modificationToken.tag
            if (anyChanged) {
                for(listener in listeners) {
                    listener.invoke()
                }
                listeners.clear()
            }

            result
        }
    }

    fun <T> applySynchronized(action: () -> T) = lock.withLock(action)
}