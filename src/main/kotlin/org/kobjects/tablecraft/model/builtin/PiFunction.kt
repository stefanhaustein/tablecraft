package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.Operation

object PiFunction : Operation {
    override fun apply(params: Map<String, Any>): Any {
        return Math.PI
    }
}