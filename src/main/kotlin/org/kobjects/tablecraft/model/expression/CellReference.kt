package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.ExpressionNode

class CellReference(
    owner: ExpressionNode,
    val target: Cell
) : Expression() {

    init {
        owner.inputs.add(target)
        target.dependencies.add(owner)
    }

    override fun eval(context: EvaluationContext): Any {
        try {
            return target.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

}
