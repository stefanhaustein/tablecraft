package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.pluginapi.*

interface Port: OperationHost, ToJson, Node {

    val name: String
    val tag: Long
    
    fun reset(simulationMode: Boolean, token: ModificationToken)

}