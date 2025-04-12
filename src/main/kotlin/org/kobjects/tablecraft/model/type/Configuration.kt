package org.kobjects.tablecraft.model.type


import org.kobjects.tablecraft.pluginapi.ParameterKind
import org.kobjects.tablecraft.pluginapi.ParameterSpec
import org.kobjects.tablecraft.svg.ParameterType

object Configuration {

    fun fromJson(
        parameters: List<ParameterSpec>,
        json: Map<String, Any>
    ): Map<String, Any> {
        TODO()
/*        val result = mutableMapOf<String, Any>()
        for (parameter in parameters.filter { it.kind == ParameterKind.CONFIGURATION }) {
            val value = json[parameter.name]
            if (value == null) {
                require(!parameter.required) {
                    "Required configuration parameter '${parameter.name}' is missing."
                }
            } else {
                result[parameter.name] = parameter.type.fromJson(value)
            }
        }

        if (json.size > result.size) {
            System.err.println("Unused keys: " + (json.keys - result.keys))
        }

        return result.toMap() */
    }

}