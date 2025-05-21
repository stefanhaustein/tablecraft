package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.CellRangeReference
import org.kobjects.tablecraft.model.CellRangeValues

class CellRangeExpression(
    owner: Cell,
    val target: CellRangeReference
) : Expression() {

    init {
        // All dDependencies are removed on re-parse. This can't be "balanced" as
        // an expression might reference the same cells multiple times
        for (t in target.iterator()) {
            owner.inputs.add(t)
            t.dependencies.add(owner)
        }
    }

    override fun eval(context: EvaluationContext): Any {
        try {
            return CellRangeValues(target)
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

}
