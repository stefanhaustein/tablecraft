package org.kobjects.pi123.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.pi123.model.Sheet
import org.kobjects.pi123.model.expression.*

object FormulaParser : PrattParser<Pi123Scanner, Sheet, Expression>(
    { scanner, sheet -> FormulaParser.parsePrimary(scanner, sheet) },
    { _, _, name, operand -> UnaryOperatorExpression(name, operand) },
    { _, _, name, leftOperand, rightOperand -> BinaryOperatorExpression(name, leftOperand, rightOperand) },
    Operator.Prefix(2, "-"),
    Operator.Infix(1, "*", "/"),
    Operator.Infix(0, "+", "-")
) {

    fun parsePrimary(scanner: Pi123Scanner, sheet: Sheet): Expression =
        when (scanner.current.type) {
            Pi123TokenType.NUMBER -> LiteralExpression(scanner.consume().text.toDouble())
            Pi123TokenType.STRING -> {
                val text = scanner.consume().text
                LiteralExpression(text.substring(1, text.length - 1))
            }
            Pi123TokenType.IDENTIFIER -> {
                val name = scanner.consume().text
                if (scanner.tryConsume("(")) {
                    val parameterList = parseParameterList(scanner, sheet)
                    when (name.lowercase()) {
                        "din" -> DigitalInputExpression(parameterList)
                        else -> FunctionCallExpression(name, parameterList)

                    }
                }
                else
                    CellReferenceExpression(sheet, name)
            }
            else -> {
                throw IllegalStateException("Unexpected token in parsePrimary ${scanner.current}")
            }

    }

    fun parseParameterList(scanner: Pi123Scanner, sheet: Sheet): Map<String, Expression> {
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
            val expression = FormulaParser.parseExpression(scanner, sheet)
            result[name] = expression
        } while (scanner.tryConsume(","))
        scanner.consume(")")
        return result.toMap()
    }


    fun parseExpression(value: String, sheet: Sheet): Expression {
        val scanner = Pi123Scanner(value)
        val result = parseExpression(scanner, sheet)
        require(scanner.eof)
        return result
    }
}