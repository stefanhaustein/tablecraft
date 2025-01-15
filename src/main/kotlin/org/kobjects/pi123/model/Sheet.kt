package org.kobjects.pi123.model

import org.json.JSONObject

class Sheet(var name: String) {
    val cells = mutableMapOf<String, Cell>()

    fun set(cellId: String, value: String) {
        val cell = cells.getOrPut(cellId) { Cell(this, cellId) }
        cell.setValue(value)
    }

    fun update() {
        for (cell in cells.values) {
            cell.updateValue()
        }
    }

    fun serializeValues(valueType: ValueType): String {
        for (cell in cells.values) {
            cell.updateValue()
        }
        val sb = StringBuilder()
        for (cell in cells.values) {
            if (sb.isNotEmpty()) {
                sb.append(", ")
            }
            val content = when (valueType) {
                ValueType.FORMULA -> cell.rawValue
                ValueType.COMPUTED_VALUE -> cell.computedValue
            }
            sb.append("${cell.id.quote()}: ${content.toString().quote()}")
        }
        return "{$sb}"
    }

    fun parseJson(jsonObject: JSONObject) {
        for (key in jsonObject.keys()) {
            set(key, jsonObject.getString(key))
        }

    }

    enum class ValueType {
        FORMULA, COMPUTED_VALUE
    }

}