package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.Cell

class CellReferenceNode(
    val source: Cell,
    val cell: Cell
) : Node() {

    override fun eval(context: EvaluationContext): Any {
        try {
            return cell.value
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Node>
        get() = emptyList()

    override fun attach() {
        super.attach()
        source.dependsOn.add(cell)
        cell.dependencies.add(source)
    }

    override fun detach() {
        super.detach()
        source.dependsOn.remove(cell)
        cell.dependencies.remove(source)
    }
}
