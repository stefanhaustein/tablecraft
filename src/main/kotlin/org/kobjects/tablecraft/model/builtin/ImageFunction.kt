package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

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