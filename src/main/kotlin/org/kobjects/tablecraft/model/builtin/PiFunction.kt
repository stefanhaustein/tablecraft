package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

object PiFunction : OperationInstance {
    override fun attach(host: OperationHost) = Unit


    override fun apply(params: Map<String, Any>): Any {
        return Math.PI
    }

    override fun detach() {
    }
}