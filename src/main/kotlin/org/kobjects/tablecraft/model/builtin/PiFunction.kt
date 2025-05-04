package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.FunctionInstance

object PiFunction : FunctionInstance {
    override fun apply(params: Map<String, Any>): Any {
        return Math.PI
    }
}