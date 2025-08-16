package org.kobjects.tablecraft.pluginapi

interface ModelInterface {

    fun applySynchronizedWithToken(action: (ModificationToken) -> Unit)

    fun addUpdateListener(permanent: Boolean, onChangeOnly: Boolean, listener: (modificationTag: Long, anyChanged: Boolean) -> Unit)

}