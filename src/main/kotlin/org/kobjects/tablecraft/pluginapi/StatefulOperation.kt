package org.kobjects.tablecraft.pluginapi

interface StatefulOperation : Operation {

    fun attach(host: OperationHost)

    fun detach()
}