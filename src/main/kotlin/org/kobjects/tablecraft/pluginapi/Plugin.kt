package org.kobjects.tablecraft.pluginapi

interface Plugin {
    fun notifySimulationModeChanged(token: ModificationToken) {

    }

    val operationSpecs: List<AbstractArtifactSpec>
}