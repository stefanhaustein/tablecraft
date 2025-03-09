package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.Model.simulationValueMap
import org.kobjects.tablecraft.pluginapi.*

abstract class Port(val name: String, val tag: Long): OperationHost, ToJson {

    abstract val value: Any

    abstract fun reset(simulationMode: Boolean, token: ModificationToken)

    abstract fun detach()

}