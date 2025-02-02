package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.ParameterKind
import org.kobjects.pi123.pluginapi.ParameterSpec
import org.kobjects.pi123.pluginapi.Type
import org.kobjects.pi123.svg.parser.ExpressionParser
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

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

    fun parameterized(params: Map<String, String>): Document {
        // TODO: Convert types

        return if (params.isEmpty()) document else parameterizedCopy(params)
    }

    fun parameterizedCopy(params: Map<String, Any>): Document {
        val copiedDocument: Document = createDocumentBuilder().newDocument()
        val copiedRoot = copiedDocument.importNode(document.documentElement, true)
        parameterize(copiedRoot as Element, params)
        copiedDocument.appendChild(copiedRoot)
        return copiedDocument
    }


    companion object {
        val PARAM_NAMESPACE = "http://kobjects.org/svg/param"

        fun createDocumentBuilder(): DocumentBuilder {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            return factory.newDocumentBuilder()
        }

        fun load(file: File, path: String): ParameterizableSvg {
            val builder = createDocumentBuilder()
            val doc: Document = builder.parse(file)
            return ParameterizableSvg(path.substring(path.lastIndexOf('.')), doc)
        }

        fun parameterize(element: Element, params: Map<String, Any>) {
            val attrs = element.attributes
            val resolved = mutableMapOf<String, String>()
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                if (attr.namespaceURI == PARAM_NAMESPACE) {
                    try {
                        val value = ExpressionParser.evaluateExpression(attr.textContent, params)
                        resolved[attr.localName] = value.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            for ((key, value) in resolved) {
                element.setAttribute(key, value)
            }

            val children = element.childNodes
            for (i in 0 until children.length) {
                val child = children.item(i)
                if (child is Element) {
                    parameterize(child, params)
                }
            }
        }
    }

}