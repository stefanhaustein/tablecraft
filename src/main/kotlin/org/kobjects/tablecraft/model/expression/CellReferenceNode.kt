package org.kobjects.tablecraft.model.expression;

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.pluginapi.RuntimeContext

class CellReferenceNode(
    val source: Cell,
    val cell: Cell
) : Node() {

    override fun eval(context: RuntimeContext): Any {
        try {
            return cell.getComputedValue(context)
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
        cell.depenencies.add(source)
    }

    override fun detach() {
        super.detach()
        source.dependsOn.remove(cell)
        cell.depenencies.remove(source)
    }
}
