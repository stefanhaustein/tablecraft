package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.Model.simulationValueMap
import org.kobjects.tablecraft.pluginapi.*

interface Port: OperationHost, ToJson {

    val name: String
    val tag: Long

    val value: Any

    fun reset(simulationMode: Boolean, token: ModificationToken)

    fun detach()

}