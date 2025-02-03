package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.Plugin
import java.io.File

class SvgManager(root: File) : Plugin {

    val map = mutableMapOf<String, ParameterizableSvg>()

    init {
        loadDirectory(root, "img/")
    }

    fun loadDirectory(dir: File, basePath: String) {
        for (file in dir.listFiles() ?: emptyArray()) {
            if (file.isDirectory) {
                loadDirectory(file, basePath + file.name + "/")
            } else if (file.name.endsWith(".svg")) {
                println("Loading file ${file.name}; local base path: $basePath")
                val path = basePath + file.name
                map[path] = ParameterizableSvg.load(file)
            }
        }
    }

    override val functionSpecs: List<FunctionSpec>
        get() {
            val result = mutableListOf<FunctionSpec>()
            for ((path, svg) in map) {
                if (svg.parameters.isNotEmpty()) {
                    val cut = path.lastIndexOf(".")
                    val spec = FunctionSpec(
                        path.substring(0, cut).replace("/", "."),
                        "Parameterized Symbol",
                        svg.parameters
                    ) { SvgFunction(path) }
                    result.add(spec)
                }
            }
            return result
        }


}