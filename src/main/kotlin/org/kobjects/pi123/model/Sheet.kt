package org.kobjects.pi123.model


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

    fun serializeValues(valueType: ValueType, toml: Boolean = false): String {
        for (cell in cells.values) {
            cell.updateValue()
        }
        val sb = StringBuilder()
        for (cell in cells.values) {
            if (sb.isNotEmpty() && !toml) {
                sb.append(", ")
            }
            val content = when (valueType) {
                ValueType.FORMULA -> cell.rawValue
                ValueType.COMPUTED_VALUE -> cell.computedValue
            }
            if (toml) {
                sb.append("${cell.id}.${valueType.name.take(1).lowercase()} = ${content.toString().quote()}\n")
            } else {
                sb.append("${cell.id.quote()}: ${content.toString().quote()}")
            }
        }
        return if (toml) sb.toString() else "{$sb}"
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
                "f" -> set(name, value.toString())
                else -> throw IllegalStateException("Unrecognized suffix in $key = $value")
            }
        }
    }

    enum class ValueType {
        FORMULA, COMPUTED_VALUE
    }

}