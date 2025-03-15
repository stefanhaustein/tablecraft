package org.kobjects.tablecraft.svg

import org.kobjects.tablecraft.model.builtin.ImageReference
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

class SvgFunction(
    val path: String,
) : OperationInstance {
    override fun attach(host: OperationHost) {

    }

    override fun apply(params: Map<String, Any>): Any {
        return ImageReference("/$path?${params.toList().map { "${it.first}=${it.second}" }.joinToString("&")}")
    }

    override fun detach() {

    }
}