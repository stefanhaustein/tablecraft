package org.kobjects.pi123.pluginapi

import org.kobjects.pi123.model.quote

data class OperationSpec(
    val kind: OperationKind,
    val returnType: Type,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val createFn: (host: OperationHost) -> OperationInstance
) {
    fun toJson(): String {
        val filteredParams = parameters.filter { kind != OperationKind.PORT_CONSTRUCTOR || it.kind != ParameterKind.RUNTIME }
        val convertedParams = filteredParams.joinToString { it.toJson() }
        return """{"name":${name.quote()},"kind":"$kind","returnType":"$returnType","description":${description.quote()},"params":[$convertedParams]}"""
    }
}

