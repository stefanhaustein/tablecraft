package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.pluginapi.ModificationToken


class Sheet(
    val name: String,
    var tag: Long = 0L
) {
    val cells = mutableMapOf<String, Cell>()
    var deleted = false

    fun set(cellId: String, value: String, modificationToken: ModificationToken) {
        val cell = getOrCreateCell(cellId)
        cell.setFormula(value, modificationToken)
    }

    fun delete(token: ModificationToken) {
        deleted = true
        tag = token.tag
        cells.clear()
        token.symbolsChanged = true
    }


    fun serialize(tag: Long, includeComputed: Boolean): String {
        val sb = StringBuilder()
        if (deleted) {
            sb.append("[sheets.$name]\n\ndeleted: true\n\n")
        } else {
            sb.append("[sheets.$name.cells]\n\n")
            for (cell in cells.values) {
                cell.serialize(sb, tag, includeComputed)
            }
        }
        return sb.toString()
    }

    fun parseToml(cells: Map<String, Any>, token: ModificationToken) {
        for ((key, value) in cells) {
            try {
                getOrCreateCell(key).setJson(value as Map<String, Any>, token)
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

}