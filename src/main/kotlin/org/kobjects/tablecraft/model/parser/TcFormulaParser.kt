package org.kobjects.tablecraft.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.*

object TcFormulaParser : PrattParser<TcScanner, ParsingContext, Expression>(
    { scanner, context -> TcFormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperatorExpression(name, operand) },
    { _, _, name, leftOperand, rightOperand -> BinaryOperatorExpression(name, leftOperand, rightOperand) },
    Operator.Prefix(2, "-"),
    Operator.Infix(1, "*", "/"),
    Operator.Infix(0, "+", "-")
) {

    fun parsePrimary(scanner: TcScanner, context: ParsingContext): Expression =
        when (scanner.current.type) {
            TcTokenType.NUMBER -> LiteralExpression(scanner.consume().text.toDouble())
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
                if (scanner.tryConsume("(")) {
                    val parameterList = parseParameterList(scanner, context)
                    when (name.lowercase()) {

                        else -> {
                            val functionSpec = Model.functionMap[name.lowercase()]
                            if (functionSpec != null) {
                                PluginOperationCallExpression.create(context.cell, functionSpec, parameterList)
                            } else {
                                BuiltinFunctionCallExpression(name, parameterList)
                            }
                        }
                    }
                }
                else when (name.lowercase()) {
                    "true" -> LiteralExpression(true)
                    "false" -> LiteralExpression(false)
                    else -> {
                        throw IllegalArgumentException("Unknown function '$name'")
                    }
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