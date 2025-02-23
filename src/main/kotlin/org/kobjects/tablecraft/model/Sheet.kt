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
            try {
                getOrCreateCell(key).setJson(value as Map<String, Any>, null)
            } catch (e: Exception) {
                System.err.println("Error parsing cell $key = $value")
                e.printStackTrace()
            }
        }
    }

    fun getOrCreateCell(cellId: String): Cell {
        return cells.getOrPut(cellId) { Cell(this, cellId) }
    }

    fun clear(runtimeContext: RuntimeContext) {
        for (cell in cells.values) {
            cell.setValue("", runtimeContext)
        }
    }

    enum class ValueType {
        FORMULA, COMPUTED_VALUE
    }

}