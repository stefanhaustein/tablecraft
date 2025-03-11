package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.Cell

class CellReference(
    val source: Cell,
    val cell: Cell
) : Expression() {

    init {
        source.dependsOn.add(cell)
        cell.dependencies.add(source)
    }

    override fun eval(context: EvaluationContext): Any {
        try {
            return cell.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

}
