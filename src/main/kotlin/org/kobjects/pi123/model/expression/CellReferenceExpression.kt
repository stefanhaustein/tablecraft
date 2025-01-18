package org.kobjects.pi123.model.expression;

import org.kobjects.pi123.model.Cell
import org.kobjects.pi123.model.RuntimeContext

class CellReferenceExpression(
    val source: Cell,
    val cell: Cell
) : Expression() {

    override fun eval(context: RuntimeContext): Any? {
        try {
            return cell.getComputedValue(context)
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()

    override fun attach() {
        super.attach()
        source.dependsOn.add(cell)
        cell.depenencies.add(source)
    }

    override fun detach() {
        super.detach()
        source.dependsOn.remove(cell)
        cell.depenencies.remove(source)
    }
}
