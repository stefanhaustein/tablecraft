package org.kobjects.tablecraft.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.CellRange
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.Sheet
import org.kobjects.tablecraft.model.expression.*


fun createBinaryOperatorNode(name: String, leftOperand: Expression, rightOperand: Expression): Expression =
    when (name) {
        "and", "AND", "And" -> LogicalOperator(LogicalOperator.LogicalOperator.AND, leftOperand, rightOperand)
        "or", "OR", "Or" -> LogicalOperator(LogicalOperator.LogicalOperator.OR, leftOperand, rightOperand)
        else -> BinaryOperator(name, leftOperand, rightOperand)
    }


object TcFormulaParser : PrattParser<TcScanner, ParsingContext, Expression>(
    { scanner, context -> TcFormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperator(name, operand) },
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

    fun parsePrimary(scanner: TcScanner, context: ParsingContext): Expression =
        when (scanner.current.type) {
            TcTokenType.NUMBER -> if (scanner.current.text.contains(".")
                || scanner.current.text.contains("e")
                || scanner.current.text.contains("E")) Literal(scanner.consume().text.toDouble()) else Literal(scanner.consume().text.toInt())
            TcTokenType.STRING -> {
                val text = scanner.consume().text
                Literal(text.substring(1, text.length - 1))
            }
            TcTokenType.CELL_IDENTIFIER -> {
                val name = scanner.consume().text
                val localName: String
                val sheet: Sheet
                val cut = name.indexOf("!")
                if (cut == -1) {
                    sheet = (context.expressionNode as Cell).sheet
                    localName = name
                } else {
                    val sheetName = name.substring(0, cut)
                    sheet = Model.sheets[sheetName] ?: throw IllegalArgumentException(
                        "Sheet '$sheetName' not found.")

                    localName = name.substring(name.indexOf('!') + 1)
                }
                if (localName.contains(":")) {
                    CellRangeReference(context.expressionNode, CellRange(sheet, localName))
                } else {
                    val cell = sheet.getOrCreateCell(localName)
                    require(context.expressionNode != cell) {
                        "Self-reference not permitted"
                    }
                    CellReference(context.expressionNode, cell)
                }
            }
            TcTokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                val parameterList = if (scanner.tryConsume("(")) parseParameterList(scanner, context) else emptyMap()
                when (name.lowercase()) {
                    "true" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        Literal(true)
                    }
                    "false" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        Literal(false)
                    }
                    else -> {
                        val functionSpec = Model.functionMap[name.lowercase()]
                        if (functionSpec != null) {
                            PluginOperationCall.create(context.expressionNode, functionSpec, parameterList)
                        } else {
                            val port = Model.portMap[name.lowercase()]
                            require(port != null) {
                                "Unresolved identifier $name"
                            }
                            PortReference(context.expressionNode, port)
                        }
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

    fun parseParameterList(scanner: TcScanner, context: ParsingContext): Map<String, Expression> {
        if (scanner.tryConsume(")")) {
            return emptyMap()
        }
        val result = mutableMapOf<String, Expression>()
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


    fun parseExpression(value: String, context: ParsingContext): Expression {
        val scanner = TcScanner(value)
        val result = parseExpression(scanner, context)
        require(scanner.eof)
        return result
    }
}