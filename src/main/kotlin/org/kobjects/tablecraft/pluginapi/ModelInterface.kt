package org.kobjects.tablecraft.pluginapi

interface ModelInterface {

    fun <T> applySynchronizedWithToken(action: (ModificationToken) -> T): T

}