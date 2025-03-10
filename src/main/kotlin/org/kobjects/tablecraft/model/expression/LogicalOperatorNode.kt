package org.kobjects.tablecraft.model.expression

class LogicalOperatorNode(
    val kind: LogicalOperator,
    val leftOperand: Node,
    val rightOperand: Node
): Node() {


    override fun eval(context: EvaluationContext): Any {
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