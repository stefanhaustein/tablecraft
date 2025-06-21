package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.expression.EvaluationContext

interface FunctionInstance {
    fun apply(context: EvaluationContext, params: Map<String, Any>): Any
}