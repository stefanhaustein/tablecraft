package org.kobjects.pi123.model.builtin

import org.kobjects.pi123.pluginapi.OperationInstance

object PiFunction : OperationInstance {
    override fun attach() {
    }

    override fun apply(params: Map<String, Any>): Any {
        return Math.PI
    }

    override fun detach() {
    }
}