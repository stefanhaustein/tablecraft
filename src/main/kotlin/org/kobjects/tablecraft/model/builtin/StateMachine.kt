package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.CellRange
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.CellRangeReference
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.StatefulOperation

class StateMachine(
    val cellRange: CellRange
) : StatefulOperation {
    var currentRow: Int = 0
    var currentState = ""

    val rowCount: Int = cellRange.toRow - cellRange.toRow + 1

    override fun attach(host: OperationHost) {

    }

    override fun detach() {

    }

    fun getValue(col: Int, row: Int): Any {
        return cellRange.sheet.getOrCreateCell(Cell.id(
            cellRange.fromColumn + col, cellRange.fromRow + row)).value
    }

    override fun apply(params: Map<String, Any>): Any {
        if (currentState == "") {
            currentState = getValue(0, 0).toString()
        }

        val initialRow = currentRow
        do {
            if (getValue(0, currentRow).toString() == currentState) {
                if (getValue(1, currentRow) == true) {
                    currentState = getValue(2, currentRow).toString()
                }
            }
            currentRow = (currentRow + 1) % rowCount
        } while (initialRow != currentRow)

        return currentState
    }



    companion object {
        fun create(configuration: Map<String, Any>) = StateMachine(
            (configuration["transitions"] as CellRangeReference).target)
    }

}