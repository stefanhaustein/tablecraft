package org.kobjects.tablecraft.svg

import org.kobjects.tablecraft.pluginapi.*
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
    override val operationSpecs: List<OperationSpec>
        get() {
            val result = mutableListOf<OperationSpec>()
            for ((path, svg) in map) {
                if (svg.parameters.isNotEmpty()) {
                    val cut = path.lastIndexOf(".")
                    val spec = OperationSpec(
                        OperationKind.FUNCTION,
                        Type.IMAGE,
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