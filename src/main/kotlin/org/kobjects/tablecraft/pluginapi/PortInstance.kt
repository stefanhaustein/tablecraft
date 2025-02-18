package org.kobjects.tablecraft.pluginapi

interface PortInstance {
    val name: String
    val tag: Long
    val operationSpecs: List<OperationSpec>


}