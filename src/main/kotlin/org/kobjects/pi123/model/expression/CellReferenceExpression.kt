package org.kobjects.pi123.model.expression;

import org.kobjects.pi123.model.Sheet

class CellReferenceExpression(
    val sheet: Sheet,
    val name: String
) : Expression() {
    override fun eval(): Any? {
        try {
            return sheet.cells[name]?.computedValue
        } catch (e: Exception) {
            e.printStackTrace()
            return e
        }
    }

    override val children: Collection<Expression>
        get() = emptyList()
}
