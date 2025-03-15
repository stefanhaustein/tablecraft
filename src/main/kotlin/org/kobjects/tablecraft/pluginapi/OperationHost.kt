package org.kobjects.tablecraft.pluginapi

interface OperationHost {

    /**
     * This does not provide a new value, as in the case of function parameters, these might have changed in the
     * current iteration as well.
     */
    fun notifyValueChanged(token: ModificationToken)
}