package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.expression.CellRangeReference
import org.kobjects.tablecraft.model.expression.CellReference
import org.kobjects.tablecraft.pluginapi.RangeValues
import java.lang.Integer.max
import java.lang.Integer.min

class CellRange(
    val sheet: Sheet,
    definition: String
): Iterable<Cell> {
    val fromColumn: Int
    val toColumn: Int   // Inclusive
    val fromRow: Int
    val toRow: Int      // Inclusive

    init {
        val cut = definition.indexOf(":")
        if (cut == -1) {
            val cell = sheet.getOrCreateCell(definition)
            fromColumn = cell.column
            fromRow = cell.row
            toColumn = cell.column
            toRow = cell.row
        } else {
            val cell1 = sheet.getOrCreateCell(definition.substring(0, cut))
            val cell2 = sheet.getOrCreateCell(definition.substring(cut + 1))

            fromColumn = min(cell1.column, cell2.column)
            fromRow = min(cell1.row, cell2.row)
            toColumn = max(cell1.column, cell2.column)
            toRow = max(cell1.row, cell2.row)
        }
    }

    val width: Int
        get() = toColumn - fromColumn + 1

    val height: Int
        get() = toRow - fromRow + 1

    operator fun get(x: Int, y: Int) =
        sheet.getOrCreateCell(Cell.id(x + fromColumn, y + fromRow))

    override fun iterator(): Iterator<Cell> {
        val result = mutableListOf<Cell>()
        for (r in fromRow..toRow) {
            for (c in fromColumn..toColumn) {
                result.add(sheet.getOrCreateCell(Cell.id(c, r)))
            }
        }
        return result.iterator()
    }

    companion object {

        fun parse(name: String, impliedSheet: Sheet? = null): CellRange {
            val cut = name.indexOf("!")
            if (cut == -1) {
                require(impliedSheet != null) {
                    "Cannot parse cell reference '${name}' without explicit sheet reference"
                }
                return CellRange(impliedSheet, name)
            }
            val sheetName = name.substring(0, cut)
            val sheet = Model.sheets[sheetName] ?: throw IllegalArgumentException(
                    "Sheet '$sheetName' not found.")

            val localName = name.substring(name.indexOf('!') + 1)
            return CellRange(sheet, localName)
        }


    }


}