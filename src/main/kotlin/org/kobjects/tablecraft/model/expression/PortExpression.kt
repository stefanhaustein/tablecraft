package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.*

class PortExpression(
    owner: Cell,
    val port: PortHolder

) : Expression() {

    init {
        owner.inputs.add(port)
        port.outputs.add(owner)
    }

    override fun eval(context: EvaluationContext): Any? {
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
