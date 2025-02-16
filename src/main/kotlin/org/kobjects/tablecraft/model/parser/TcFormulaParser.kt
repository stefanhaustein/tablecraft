package org.kobjects.tablecraft.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.*

object TcFormulaParser : PrattParser<TcScanner, ParsingContext, Expression>(
    { scanner, context -> TcFormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperatorExpression(name, operand) },
    { _, _, name, leftOperand, rightOperand -> BinaryOperatorExpression(name, leftOperand, rightOperand) },
    Operator.Infix(7, "."),
    Operator.Prefix(6, "-"),
    Operator.Suffix(5, "%"),
    Operator.Infix(4, "^"),
    Operator.Infix(3, "*", "/"),
    Operator.Infix(2, "+", "-"),
    Operator.Infix(1, "&"),
    Operator.Infix(0, "=", "<>", "<=", "=>", "<", ">")
) {

    fun parsePrimary(scanner: TcScanner, context: ParsingContext): Expression =
        when (scanner.current.type) {
            TcTokenType.NUMBER -> if (scanner.current.text.contains(".")
                || scanner.current.text.contains("e")
                || scanner.current.text.contains("E")) LiteralExpression(scanner.consume().text.toDouble()) else LiteralExpression(scanner.consume().text.toInt())
            TcTokenType.STRING -> {
                val text = scanner.consume().text
                LiteralExpression(text.substring(1, text.length - 1))
            }
            TcTokenType.CELL_IDENTIFIER -> {
                val cell = context.cell.sheet.getOrCreateCell(scanner.consume().text)
                require(context.cell != cell) {
                    "Self-reference not permitted"
                }
                CellReferenceExpression(context.cell, cell)
            }
            TcTokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                val parameterList = if (scanner.tryConsume("(")) parseParameterList(scanner, context) else emptyMap()
                when (name.lowercase()) {
                    "true" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        LiteralExpression(true)
                    }
                    "false" -> {
                        require(parameterList.isEmpty()) {
                            "Unexpected parameter(s) for 'TRUE'"
                        }
                        LiteralExpression(false)
                    }
                    else -> {
                        val functionSpec = Model.functionMap[name.lowercase()]
                        require (functionSpec != null) {
                            "Unresolved function '$name'"
                        }
                        PluginOperationCallExpression.create(context.cell, functionSpec, parameterList)
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