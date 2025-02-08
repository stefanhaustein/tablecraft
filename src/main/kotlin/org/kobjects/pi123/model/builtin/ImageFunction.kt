package org.kobjects.pi123.model.builtin

import org.kobjects.pi123.pluginapi.OperationHost
import org.kobjects.pi123.pluginapi.OperationInstance

class ImageFunction(source: String) : OperationInstance {

    val image = ImageReference(source)

    override fun attach() {
    }

    override fun apply(params: Map<String, Any>) = image

    override fun detach() {
    }


    companion object {
        fun create(host: OperationHost) = ImageFunction(host.configuration["source"] as String)
    }
}