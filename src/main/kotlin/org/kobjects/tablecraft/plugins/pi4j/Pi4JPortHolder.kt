package org.kobjects.tablecraft.plugins.pi4j

/**
 * Workaround for https://github.com/Pi4J/pi4j/issues/440, which prevents port
 * reuse. This interface allows the plugin to shut down the whole pi4j context and
 * then re-instantiate all known ports by calling attachPort().
 */
interface Pi4JPortHolder {
    fun attachPort()
    fun detachPort()
}