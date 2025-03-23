package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.json.ToJson

interface Integration : ToJson {
    val operationSpecs: List<OperationSpec>
    val tag: Long

    fun detach()


}