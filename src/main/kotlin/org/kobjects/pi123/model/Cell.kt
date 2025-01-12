package org.kobjects.pi123.model

import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent
import com.pi4j.io.gpio.digital.PullResistance
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
                val p0 = params[0].trim()
                val cut = p0.indexOf("=")
                val address = p0.substring(cut + 1).trim().toInt()
                val config = DigitalInput.newConfigBuilder(Model.pi4J)
                    .address(address)
                    .pull(PullResistance.PULL_DOWN)
                    .debounce(1000L)

                val digitalInput = Model.pi4J.create(config)
                /*val listener = digitalInput.addListener({

                    println("event: $it input: $digitalInput")

                })*/
                computeFn = {
                    println("DigitalInput $address state ${digitalInput.state()}; $digitalInput")
                    digitalInput.state()
                }
                clearFn = { digitalInput.shutdown(Model.pi4J) }
            }
        }
    }

    fun updateValue() {
        computedValue = computeFn()
    }


}