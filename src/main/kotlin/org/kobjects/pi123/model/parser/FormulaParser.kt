package org.kobjects.pi123.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.pi123.model.Sheet
import org.kobjects.pi123.model.expression.*

object FormulaParser : PrattParser<Pi123Scanner, ParsingContext, Expression>(
    { scanner, context -> FormulaParser.parsePrimary(scanner, context) },
    { _, _, name, operand -> UnaryOperatorExpression(name, operand) },
    { _, _, name, leftOperand, rightOperand -> BinaryOperatorExpression(name, leftOperand, rightOperand) },
    Operator.Prefix(2, "-"),
    Operator.Infix(1, "*", "/"),
    Operator.Infix(0, "+", "-")
) {

    fun parsePrimary(scanner: Pi123Scanner, context: ParsingContext): Expression =
        when (scanner.current.type) {
            Pi123TokenType.NUMBER -> LiteralExpression(scanner.consume().text.toDouble())
            Pi123TokenType.STRING -> {
                val text = scanner.consume().text
                LiteralExpression(text.substring(1, text.length - 1))
            }
            Pi123TokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                if (scanner.tryConsume("(")) {
                    val parameterList = parseParameterList(scanner, context)
                    when (name.lowercase()) {
                        "din" -> DigitalInputExpression(context.cell, parameterList)
                        "pwmin" -> PwmInputExpression(context.cell, parameterList)
                        "dout" -> DigitalOutputExpression(parameterList)
                        "now" -> NowExpression(context, parameterList)
                        else -> FunctionCallExpression(name, parameterList)
                    }
                }
                else {
                    val cell = context.cell.sheet.getOrCreateCell(name)
                    require(context.cell != cell) {
                        "Self-reference not permitted"
                    }
                    CellReferenceExpression(context.cell, cell)
                }
            }
            else -> {
                throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
            }

    }

    fun parseParameterList(scanner: Pi123Scanner, context: ParsingContext): Map<String, Expression> {
        if (scanner.tryConsume(")")) {
            return emptyMap()
        }
        val result = mutableMapOf<String, Expression>()
        var index = 0
        do {
            var name = (index++).toString()
            if (scanner.current.type == Pi123TokenType.IDENTIFIER &&
                scanner.lookAhead(1).text == "=") {
                name = scanner.consume().text
                scanner.consume("=")
            }
            val expression = FormulaParser.parseExpression(scanner, context)
            result[name] = expression
        } while (scanner.tryConsume(","))
        scanner.consume(")")
        return result.toMap()
    }


    fun parseExpression(value: String, context: ParsingContext): Expression {
        val scanner = Pi123Scanner(value)
        val result = parseExpression(scanner, context)
        require(scanner.eof)
        return result
    }
}