package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.ToJson
import org.kobjects.tablecraft.pluginapi.*

// Can't be an abstract class because ExpressionNode already is a superclass of OutputPortHolder.
interface PortHolder: ValueChangeListener, ToJson, Node {

    val name: String
    val tag: Long

    fun reset(simulationMode: Boolean, token: ModificationToken)

    override fun qualifiedId() = name
}