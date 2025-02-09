package org.kobjects.tablecraft.model


class Sheet(var name: String) {
    val cells = mutableMapOf<String, Cell>()

    fun set(cellId: String, value: String, runtimeContext: RuntimeContext?) {
        val cell = getOrCreateCell(cellId)
        cell.setValue(value, runtimeContext)
    }

    fun updateAll(context: RuntimeContext) {
        for (cell in cells.values) {
            cell.getComputedValue(context)
        }
    }

    fun serialize(tag: Long, includeComputed: Boolean): String {
        val sb = StringBuilder("[sheets.$name.cells]\n\n")
        for (cell in cells.values) {
            cell.serialize(sb, tag, includeComputed)
        }
        return sb.toString()
    }

    fun parseToml(cells: Map<String, Any>) {
        for ((key, value) in cells) {
            val cut = key.indexOf(".")
            val name: String
            val suffix: String
            if (cut == -1) {
                name = key
                suffix = "f"
            } else {
                name = key.substring(0, cut)
                suffix = key.substring(cut + 1)
            }
            when (suffix) {
                "f" -> set(name, value.toString(), null)
                else -> throw IllegalStateException("Unrecognized suffix in $key = $value")
            }
        }
    }

    fun getOrCreateCell(cellId: String): Cell {
        return cells.getOrPut(cellId) { Cell(this, cellId) }
    }

    enum class ValueType {
        FORMULA, COMPUTED_VALUE
    }

}