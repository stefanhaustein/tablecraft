package org.kobjects.pi123.model

import com.pi4j.io.gpio.digital.DigitalInput
import java.time.LocalDateTime

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var value_: String = ""
    var computedValue: Any? = null

    var computeFn: () -> Any = {""}
    var clearFn: () -> Unit = {}

    fun clear() {
        computeFn = {""}
        clearFn()
        clearFn = {}
    }

    fun setValue(value: String) {
        if (value.isNotEmpty()) {
            clear()
        }
        value_ = value
        if (!value.startsWith("=")) {
            computeFn = { value }
        } else {
            setFormula(value.substring(1))
        }
    }

    fun setFormula(value: String) {
        val cut0 = value.indexOf('(')
        val cut1 = value.lastIndexOf(')')
        val params = if (cut0 == -1 || cut1 == -1) emptyList<String>() else
            value.substring(cut0 + 1, cut1).split(",")
        val name = if (cut0 == -1) value else value.take(cut0)
        when (name) {
            "time" -> computeFn = { LocalDateTime.now() }
            "din" -> {
                val cfg = DigitalInput.newConfigBuilder(Model.pi4J).address(params[0].trim().toInt()).build()
                val digitalInput = Model.pi4J.create(cfg)
                computeFn = { digitalInput.isOn }
                clearFn = { digitalInput.shutdown(Model.pi4J) }
            }
        }
    }

    fun updateValue() {
        computedValue = computeFn()
    }


}