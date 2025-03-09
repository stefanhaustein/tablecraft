package org.kobjects.tablecraft.model.expression

import org.kobjects.tablecraft.pluginapi.ModificationToken

class LogicalOperatorNode(
    val kind: LogicalOperator,
    val leftOperand: Node,
    val rightOperand: Node
): Node() {


    override fun eval(context: ModificationToken): Any {
        val left = leftOperand.evalBoolean(context)
        return when (kind) {
            LogicalOperator.AND -> left && rightOperand.evalBoolean(context)
            LogicalOperator.OR -> left || rightOperand.evalBoolean(context)
        }
    }

    override val children: Collection<Node>
        get() = listOf(leftOperand, rightOperand)


    enum class LogicalOperator {
        AND, OR
    }

}