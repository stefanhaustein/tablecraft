package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.OperationInstance

object PiFunction : OperationInstance {
    override fun attach() {
    }

    override fun apply(params: Map<String, Any>): Any {
        return Math.PI
    }

    override fun detach() {
    }
}