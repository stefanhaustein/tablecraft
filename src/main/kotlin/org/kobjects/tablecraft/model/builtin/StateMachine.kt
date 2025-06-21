package org.kobjects.tablecraft.model.builtin

import org.kobjects.tablecraft.model.Cell
import org.kobjects.tablecraft.model.CellRangeReference
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.expression.CellRangeExpression
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.ModificationToken
import org.kobjects.tablecraft.pluginapi.ValueChangeListener
import org.kobjects.tablecraft.pluginapi.StatefulFunctionInstance
import java.util.*

class StateMachine(
    val cellRange: CellRangeReference
) : StatefulFunctionInstance {
    private var currentState = ""
    private val highlighted = mutableSetOf<CellRangeReference>()

    var currentRow: Int = 0

    val timer = Timer()
    var timedTransition: TimedTransition? = null
    var host: ValueChangeListener? = null

    val rowCount: Int = cellRange.toRow - cellRange.fromRow + 1

    override fun attach(host: ValueChangeListener) {
        this.host = host
    }

    override fun detach() {
        host = null
    }

    fun getValue(col: Int, row: Int): Any {
        return cellRange.sheet.getOrCreateCell(Cell.id(
            (if (col < 0) cellRange.toColumn + 1 else cellRange.fromColumn) + col, cellRange.fromRow + row)).value
    }


    fun setCurrentState(token: ModificationToken, state: String) {
        if (state == currentState) {
            return
        }
        for (highlight in highlighted) {
            cellRange.sheet.setHighlight(token, highlight, false)
        }
        for (i in 0 until rowCount) {
            if (getValue(0, i) == state) {
                val toHighlight = CellRangeReference(cellRange.sheet, cellRange.fromColumn, cellRange.fromRow + i, cellRange.toColumn, cellRange.fromRow + i)
                highlighted.add(toHighlight)
                cellRange.sheet.setHighlight(token, toHighlight, true)
            }
        }
        currentState = state
    }

    override fun apply(context: EvaluationContext, params: Map<String, Any>): Any {
        println("**** state machine apply called; currentState: $currentState")

        if (currentState == "") {
            setCurrentState(context.token, getValue(0, 0).toString())
        }

        val initialRow = currentRow
        do {
            if (getValue(0, currentRow).toString() == currentState) {
                val gate = getValue(-2, currentRow)
                if (gate == true) {
                    timedTransition?.cancel()
                    timedTransition = null
                    setCurrentState(context.token, getValue(-1, currentRow).toString())

                    // Trigger a re-calculation as there might be a followup state transition
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            Model.applySynchronizedWithToken {
                                host?.notifyValueChanged(it)
                            }
                        }
                    }, 1)
                    break
                } else if (gate is Number) {
                    val delay = gate.toLong()
                    val targetTime = System.currentTimeMillis() + delay
                    if (targetTime < timedTransition?.targetTime ?: Long.MAX_VALUE) {
                        timedTransition?.cancel()
                        val newTransition = TimedTransition(
                            targetTime, getValue(-1, currentRow).toString(), currentRow)
                        timedTransition = newTransition
                        timer.schedule(newTransition, delay)
                    }
                }
            }

            currentRow = (currentRow + 1) % rowCount
        } while (initialRow != currentRow)

        println("**** state machine apply result: $currentState")

        return currentState
    }


    inner class TimedTransition(
        val targetTime: Long,
        val targetState: String,
        val targetRow: Int,
    ) : TimerTask() {
        override fun run() {
            Model.applySynchronizedWithToken {
                timedTransition = null
                setCurrentState(it, targetState)
                currentRow = targetRow
                host?.notifyValueChanged(it)
            }
        }
    }



    companion object {
        fun create(configuration: Map<String, Any>) = StateMachine(
            (configuration["transitions"] as CellRangeExpression).target)
    }

}