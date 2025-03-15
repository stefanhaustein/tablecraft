package org.kobjects.tablecraft.pluginapi

interface OperationHost {
    val configuration: Map<String, Any>

    /**
     * This does not provide a new value, as in the case of function parameters, these might have changed in the
     * current iteration as well.
     */
    fun notifyValueChanged(newValue: Any, token: ModificationToken)
}