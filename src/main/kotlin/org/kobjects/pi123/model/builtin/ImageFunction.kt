package org.kobjects.pi123.model.builtin

import org.kobjects.pi123.pluginapi.FunctionHost
import org.kobjects.pi123.pluginapi.FunctionInstance

class ImageFunction(source: String) : FunctionInstance {

    val image = ImageReference(source)

    override fun attach() {
    }

    override fun apply(params: Map<String, Any>) = image

    override fun detach() {
    }


    companion object {
        fun create(host: FunctionHost) = ImageFunction(host.configuration["source"] as String)
    }
}