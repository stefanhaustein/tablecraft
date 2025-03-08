package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.pluginapi.ModificationToken


class Sheet(var name: String) {
    val cells = mutableMapOf<String, Cell>()

    fun set(cellId: String, value: String, modificationToken: ModificationToken?) {
        val cell = getOrCreateCell(cellId)
        cell.setFormula(value, modificationToken)
    }

    fun updateAll(context: ModificationToken) {
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

    fun clear(modificationToken: ModificationToken) {
        for (cell in cells.values) {
            cell.setFormula("", modificationToken)
        }
    }

    enum class ValueType {
        FORMULA, COMPUTED_VALUE
    }

}