package org.kobjects.tablecraft.plugins.homeassistant

import org.kobjects.tablecraft.pluginapi.AbstractArtifactSpec
import org.kobjects.tablecraft.pluginapi.ModelInterface
import org.kobjects.tablecraft.pluginapi.Plugin

class HomeAssistantPlugin(val model: ModelInterface) : Plugin {
    override val operationSpecs = listOf<AbstractArtifactSpec>(
        HomeAssistantIntegration.spec(model)
    )
}