package org.kobjects.pi123.model.parser

import org.kobjects.parsek.expression.Operator
import org.kobjects.parsek.expression.PrattParser
import org.kobjects.pi123.model.expression.BinaryOperatorExpression
import org.kobjects.pi123.model.expression.Expression
import org.kobjects.pi123.model.expression.LiteralExpression
import org.kobjects.pi123.model.expression.UnaryOperatorExpression

object FormulaParser : PrattParser<Pi123Scanner, Unit, Expression>(
    { scanner, _ -> FormulaParser.parsePrimary(scanner) },
    { _, _, name, operand -> UnaryOperatorExpression(name, operand) },
    { _, _, name, leftOperand, rightOperand -> BinaryOperatorExpression(name, leftOperand, rightOperand) },
    Operator.Prefix(2, "-"),
    Operator.Infix(1, "*", "/"),
    Operator.Infix(0, "+", "-")
) {

    fun parsePrimary(scanner: Pi123Scanner): Expression {
        return LiteralExpression(scanner.consume(Pi123TokenType.NUMBER).text.toDouble())
    }

}