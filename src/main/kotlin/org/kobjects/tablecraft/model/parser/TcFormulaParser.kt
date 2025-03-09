package org.kobjects.tablecraft.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.*


fun createBinaryOperatorNode(name: String, leftOperand: Node, rightOperand: Node): Node =
    when (name) {
        "and", "AND", "And" -> LogicalOperatorNode(LogicalOperatorNode.LogicalOperator.AND, leftOperand, rightOperand)
        "or", "OR", "Or" -> LogicalOperatorNode(LogicalOperatorNode.LogicalOperator.OR, leftOperand, rightOperand)
        else -> BinaryOperatorNode(name, leftOperand, rightOperand)
    }


object TcFormulaParser : PrattParser<TcScanner, ParsingContext, Node>(
    { scanner, context -> TcFormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperatorNode(name, operand) },
    { _, _, name, leftOperand, rightOperand -> createBinaryOperatorNode(name, leftOperand, rightOperand) },
    Operator.Prefix(10, "not", "NOT", "Not"),
    Operator.Infix(9, "."),
    Operator.Prefix(8, "-"),
    Operator.Suffix(7, "%"),
    Operator.Infix(6, "^"),
    Operator.Infix(5, "*", "/"),
    Operator.Infix(4, "+", "-"),
    Operator.Infix(3, "&"),
    Operator.Infix(2, "=", "<>", "<=", "=>", "<", ">"),
    Operator.Infix(1, "and", "AND", "And"),
    Operator.Infix(0, "or", "OR", "Or")
) {

    fun parsePrimary(scanner: TcScanner, context: ParsingContext): Node =
        when (scanner.current.type) {
            TcTokenType.NUMBER -> if (scanner.current.text.contains(".")
                || scanner.current.text.contains("e")
                || scanner.current.text.contains("E")) LiteralNode(scanner.consume().text.toDouble()) else LiteralNode(scanner.consume().text.toInt())
            TcTokenType.STRING -> {
                val text = scanner.consume().text
                LiteralNode(text.substring(1, text.length - 1))
            }
            TcTokenType.CELL_IDENTIFIER -> {
                val cell = (context.expressionHolder as Cell).sheet.getOrCreateCell(scanner.consume().text)
                require(context.expressionHolder != cell) {
                    "Self-reference not permitted"
                }
                CellReferenceNode(context.expressionHolder, cell)
            }
            TcTokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                val parameterList = if (scanner.tryConsume("(")) parseParameterList(scanner, context) else emptyMap()
                when (name.lowercase()) {
                    "true" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        LiteralNode(true)
                    }
                    "false" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        LiteralNode(false)
                    }
                    else -> {
                        val functionSpec = Model.functionMap[name.lowercase()]
                        require (functionSpec != null) {
                            "Unresolved function '$name'"
                        }
                        PluginOperationCallNode.create(context.expressionHolder, functionSpec, parameterList)
                    }
                }
            }
            TcTokenType.SYMBOL -> {
                if (scanner.tryConsume("(")) {
                    val result = parsePrimary(scanner, context)
                    scanner.consume(")")
                    result
                } else {
                    throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
                }
            }
            else -> {
                throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
            }

    }

    fun parseParameterList(scanner: TcScanner, context: ParsingContext): Map<String, Node> {
        if (scanner.tryConsume(")")) {
            return emptyMap()
        }
        val result = mutableMapOf<String, Node>()
        var index = 0
        do {
            var name = (index++).toString()
            if (scanner.current.type == TcTokenType.IDENTIFIER &&
                scanner.lookAhead(1).text == "=") {
                name = scanner.consume().text
                scanner.consume("=")
            }
            val expression = TcFormulaParser.parseExpression(scanner, context)
            result[name] = expression
        } while (scanner.tryConsume(","))
        scanner.consume(")")
        return result.toMap()
    }


    fun parseExpression(value: String, context: ParsingContext): Node {
        val scanner = TcScanner(value)
        val result = parseExpression(scanner, context)
        require(scanner.eof)
        return result
    }
}