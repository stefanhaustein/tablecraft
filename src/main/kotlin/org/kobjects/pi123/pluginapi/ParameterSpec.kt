package org.kobjects.pi123.pluginapi

import org.kobjects.pi123.model.quote

data class ParameterSpec(
    val name: String,
    val kind: ParameterKind,
    val type: Type
) {

    fun toJson(): String =
        """{"name":${name.quote()},"kind":${kind.toString().quote()},"type":${type.toString().quote()}}""".trimMargin()

}