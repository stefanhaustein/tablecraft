package org.kobjects.tablecraft.model.type


import org.kobjects.tablecraft.pluginapi.ParameterSpec

object Configuration {

    fun fromJson(
        parameters: List<ParameterSpec>,
        json: Map<String, Any>
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (parameter in parameters) {
            val value = json[parameter.name]
            if (value == null) {
                require(!parameter.modifiers.contains(ParameterSpec.Modifier.OPTIONAL)) {
                    "Required configuration parameter '${parameter.name}' is missing."
                }
            } else if (parameter.modifiers.contains(ParameterSpec.Modifier.CONSTANT)) {
                result[parameter.name] =  parameter.type.valueFromJson(value)
            } else {
                result[parameter.name] = value
            }
        }

        if (json.size > result.size) {
            val unexpectedKeys = json.keys - result.keys
            System.err.println("Unused keys: $unexpectedKeys")
            for (key in unexpectedKeys) {
                result[key] = json[key]!!
            }
        }
        return result.toMap()
    }

}