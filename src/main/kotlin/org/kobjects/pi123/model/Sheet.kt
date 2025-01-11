package org.kobjects.pi123.model

class Sheet(var name: String) {
    val cells = mutableMapOf<String, Cell>()

    fun set(cellId: String, value: String) {
        val cell = cells.getOrPut(cellId) { Cell(this, cellId) }
        cell.setValue(value)
    }

    fun getComputedValues(): String {
        for (cell in cells.values) {
            cell.updateValue()
        }
        val sb = StringBuilder()
        for (cell in cells.values) {
            if (sb.isNotEmpty()) {
                sb.append(", ")
            }
            sb.append(""""${cell.id}": "${cell.computedValue}"""")
        }
        return "{$sb}"
    }


}