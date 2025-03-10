package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.*

class PortReferenceNode(
    val source: Expression,
    val port: Port

) : Node() {

    override fun eval(context: EvaluationContext): Any {
        try {
            return port.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Node>
        get() = emptyList()

    override fun attach() {
        super.attach()
        when (port) {
            is OutputPort -> {
                source.dependsOn.add(port.expression)
                port.expression.dependencies.add(source)
            }
            is InputPort -> {
                port.dependencies.add(source)
            }
        }
    }

    override fun detach() {
        super.detach()
        when (port) {
            is OutputPort -> {
                source.dependsOn.remove(port.expression)
                port.expression.dependencies.remove(source)
            }
            is InputPort -> {
                port.dependencies.remove(source)
            }
        }
    }
}
