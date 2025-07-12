package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*

class RestOut(val fieldName: String) : OutputPortInstance {
    override fun attach() {
    }

    override fun detach() {
        Model.restValues.remove(fieldName)
    }

    override fun setValue(value: Any) {
        if (value != null) {
            Model.restValues[fieldName] = value
        } else {
            Model.restValues.remove(fieldName)
        }
    }

    companion object {
        val SPEC = OutputPortSpec(
            "Network",
            Type.STRING,
            "rest.out",
            """Makes the given value accessible via an JSON object under the '/rest' path of this server.""",
            listOf(ParameterSpec("fieldName", Type.STRING, setOf(ParameterSpec.Modifier.CONSTANT))),
            setOf(AbstractArtifactSpec.Modifier.NO_SIMULATION)
        ) {
            RestOut(it["fieldName"] as String)
        }
    }
}