package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class OutputPortHolder(
    override val name: String,
    val specification: OutputPortSpec,
    val configuration: Map<String, Any>,
    val rawFormula: String,
    override val tag: Long
) : /*ExpressionNode(),*/Node,  PortHolder {
    val instance = specification.createFn(configuration)
    var error: Exception? = null
    var attached = false
    var cellRange: CellRangeReference? = null
    var singleCell = false
    override var value: Any = Unit
    override var valueTag: Long = tag

    override val dependencies = mutableSetOf<Node>()
    override val inputs = mutableSetOf<Node>()


    override fun updateValue(token: ModificationToken): Boolean {
        value = if (singleCell) cellRange!!.iterator().next().value else CellRangeValues(cellRange!!)
        valueTag = token.tag
        if (attached) {
            try {
                instance.setValue(value)
                error = null
            } catch (e: Exception) {
                e.printStackTrace()
                error = e
            }
            return true
        }
        return false
    }
    /*    }
        if (super.updateValue(tag)) {
            if (attached) {
                try {
                    instance.setValue(value)
                    error = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    error = e
                }
            }
            true
        } else false*/

    fun reparse() {
        singleCell = !rawFormula.contains(":")
        val rawReference = if (rawFormula.startsWith("=")) rawFormula.substring(1) else rawFormula
        cellRange = CellRangeReference.parse(rawReference)

        clearDependsOn()
        for (cell in cellRange!!) {
            inputs.add(cell)
            cell.dependencies.add(this)
        }
    }

    fun clearDependsOn() {
        for (dep in inputs) {
            dep.dependencies.remove(this)
        }
        inputs.clear()
    }

    override fun reset(simulationMode: Boolean, token: ModificationToken) {
        detach()

        reparse()

        if (!simulationMode || specification.modifiers.contains(AbstractArtifactSpec.Modifier.NO_SIMULATION)) {
            try {
                instance.attach()
                attached = true
            } catch (exception: Exception) {
                error = exception
                exception.printStackTrace()
            }
        }
    }

    override fun detach() {

        if (attached) {
            try {
                instance.detach()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            attached = false
        }
    }

    override fun notifyValueChanged(token: ModificationToken) {
        System.out.println("Unexpected change notification in Output Port")
    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":${specification.name.quote()}, "configuration": """)
        configuration.toJson(sb)
        serializeDependencies(sb)

        sb.append(""", "source":${rawFormula.quote()}}""")

    }


    override fun toString() = name
}