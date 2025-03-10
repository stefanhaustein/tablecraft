package org.kobjects.tablecraft.model.expression

class LiteralNode(val value: Any) : Node() {


    override fun eval(context: EvaluationContext) = value

    override val children: Collection<Node>
        get() = emptyList()
}