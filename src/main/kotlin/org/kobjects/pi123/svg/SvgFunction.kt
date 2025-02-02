package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.FunctionInstance

class SvgFunction(
    val path: String,
    val parameterizableSvg: ParameterizableSvg
) : FunctionInstance {
    override fun attach() {

    }

    override fun apply(params: Map<String, Any>): Any {
        TODO("Not yet implemented")
    }

    override fun detach() {

    }
}