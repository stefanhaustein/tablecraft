package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.FunctionSpec
import org.kobjects.pi123.pluginapi.ParameterKind
import org.kobjects.pi123.pluginapi.ParameterSpec
import org.kobjects.pi123.pluginapi.Type
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

const val PARAM_NAMESPACE = "http://kobjects.org/svg/param"

class ParameterizableSvg(
    val path: String,
    val document: Document
) {

    fun getParameters(): List<ParameterSpec> {
        val result = mutableListOf<ParameterSpec>()
        val childNodes = document.documentElement.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName.equals("def") && child.namespaceURI == PARAM_NAMESPACE) {
                val spec = ParameterSpec(
                        child.attributes.getNamedItem("name")!!.nodeValue,
                        ParameterKind.RUNTIME,
                        Type.DOUBLE)
                result.add(spec)
            }
        }
        return result
    }



    companion object {
        fun load(file: File, path: String): ParameterizableSvg {
            val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc: Document = builder.parse(file)
            return ParameterizableSvg(path.substring(path.lastIndexOf('.')), doc)
        }
    }

}