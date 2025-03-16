package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.Operation

class ImageFunction(val source: String) : Operation {

    val image = ImageReference(source)

    override fun apply(params: Map<String, Any>) = ImageReference(source)


    companion object {
        fun create(configuration: Map<String, Any>) = ImageFunction(configuration["source"] as String)
    }
}