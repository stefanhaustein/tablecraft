package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.pluginapi.*

class RestOut(val fieldName: String) : StatefulOperation {
    override fun attach(host: OperationHost) {
    }

    override fun detach() {
        Model.restValues.remove(fieldName)
    }

    override fun apply(params: Map<String, Any>): Any {
        val value = params["value"]
        if (value != null) {
            Model.restValues[fieldName] = value
        } else {
            Model.restValues.remove(fieldName)
        }
        return value ?: Unit
    }

    companion object {
        val SPEC = OperationSpec(
            OperationKind.OUTPUT_PORT,
            Type.TEXT,
            "rest.out",
            """Makes the given value accessible via an JSON object under the '/rest' path of this server.""",
            listOf(ParameterSpec("fieldName", Type.TEXT, setOf(ParameterSpec.Modifier.CONSTANT))),
            setOf(OperationSpec.Modifier.NO_SIMULATION)
        ) {
            RestOut(it["fieldName"] as String)
        }
    }
}