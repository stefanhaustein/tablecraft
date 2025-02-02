package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.Plugin
import java.io.File

class SvgManager(root: File) : Plugin {

    val map = mutableMapOf<String, ParameterizableSvg>()

    init {
        loadDirectory(root)
    }

    fun loadDirectory(dir: File, basePath: String = "") {
        for (file in dir.listFiles() ?: emptyArray()) {
            if (file.isDirectory) {
                loadDirectory(file, basePath + file.name + "/")
            } else if (file.name.endsWith(".svg")) {
                println("Loading file ${file.name}; local base path: $basePath")
                val path = basePath + file.name
                map[path] = ParameterizableSvg.load(file, path)
            }
        }
    }

    override val functionSpecs: List<FunctionSpec>
        get() {
            val result = mutableListOf<FunctionSpec>()
            for ((name, svg) in map) {
                val spec = FunctionSpec(
                    name.replace("/", "."),
                    "Image",
                    svg.getParameters()) { SvgFunction(name, svg) }
                result.add(spec)
            }
            return result
        }


}