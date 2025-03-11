package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.*

class PortReference(
    val source: ExpressionNode,
    val port: Port

) : Expression() {

    init {
        when (port) {
            is OutputPort -> {
                source.dependsOn.add(port)
                port.dependencies.add(source)
            }
            is InputPort -> {
                port.dependencies.add(source)
            }
        }
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
