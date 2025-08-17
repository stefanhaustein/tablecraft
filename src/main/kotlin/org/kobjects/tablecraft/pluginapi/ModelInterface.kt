package org.kobjects.tablecraft.pluginapi

interface ModelInterface {

    fun applySynchronizedWithToken(callback: ((modificationTag: Long, anyChanged: Boolean) -> Unit)? = null, action: (ModificationToken) -> Unit)

    fun addUpdateListener(permanent: Boolean, onChangeOnly: Boolean, listener: (modificationTag: Long, anyChanged: Boolean) -> Unit)

    /**
     * Runs the given task once.
     */
    fun runAsync(delay: Long = 0, task: () -> Unit)

    /**
     * Schedules the given task to run asynchronously with the given interval in ms until the task returns false
     */
    fun scheduleAsync(interval: Long, initialDelay: Long = 0, task: () -> Boolean)

}