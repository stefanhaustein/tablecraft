package org.kobjects.tablecraft.pluginapi

interface Plugin {
    val portSpecs: List<PortSpec>
    val operationSpecs: List<OperationSpec>
}