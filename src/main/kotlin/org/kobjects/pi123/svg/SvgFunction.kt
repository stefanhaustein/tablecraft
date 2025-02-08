package org.kobjects.pi123.svg

import org.kobjects.pi123.model.builtin.ImageReference
import org.kobjects.pi123.pluginapi.OperationInstance

class SvgFunction(
    val path: String,
) : OperationInstance {
    override fun attach() {

    }

    override fun apply(params: Map<String, Any>): Any {
        return ImageReference("/$path?${params.toList().map { "${it.first}=${it.second}" }.joinToString("&")}")
    }

    override fun detach() {

    }
}