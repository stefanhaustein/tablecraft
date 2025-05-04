package org.kobjects.tablecraft.pluginapi

interface StatefulFunctionInstance : FunctionInstance {

    fun attach(host: ValueChangeListener)

    fun detach()
}