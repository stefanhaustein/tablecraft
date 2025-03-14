package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.*

class PortReference(
    owner: ExpressionNode,
    val port: Port

) : Expression() {

    init {
        owner.dependsOn.add(port)
        port.dependencies.add(owner)
    }

    override fun eval(context: EvaluationContext): Any {
        try {
            return port.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()


}
