package moe.haruue.gradle.plugin.test

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.xmlpull.mxp1.MXParserFactory
import org.xmlpull.v1.XmlPullParser.*
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.util.*

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class TestPlugin : Plugin<Project> {

    companion object {
        val NS_ANDROID = "http://schemas.android.com/apk/res/android"
        val NS_APP = "http://schemas.android.com/apk/res-auto"
        val NS_TOOLS = "http://schemas.android.com/tools"
    }

    val xmlPullParser by lazy {
        val factory = MXParserFactory.newInstance()
        with(factory) {
            isNamespaceAware = true
        }
        factory.newPullParser()
    }

    override fun apply(project: Project) {
        log("apply(${project.name})")
        project.extensions.findByType(AppExtension::class.java).run {
            val res = sourceSets.map { it.res }.flatMap { it.sourceFiles }
            val layouts = res.filter {
                if (it.extension == "xml") {
                    return@filter it.isLayout()
                }
                return@filter false
            }
            log("layouts: $layouts")
            layouts.forEach {
                val result = layout(it).toString()
                log("xml parse result for $it: \n$result")
            }

        }
    }

    private fun File.isLayout(): Boolean {
        xmlPullParser.setInput(reader())
        var result = false
        try {
            parse@ while (true) {
                val eventType = xmlPullParser.next()
                when (eventType) {
                    START_DOCUMENT -> {}
                    START_TAG -> {
                        val v = xmlPullParser.getAttributeValue(NS_TOOLS, "convert")
                        if (v == "true") {
                            result = true
                            break@parse
                        }
                    }
                    END_DOCUMENT -> break@parse
                }
            }
        } catch (e: XmlPullParserException) {
            err("failure in parse xml file: $this", e)
        }
        return result
    }

    class XmlAttr(val name: String,
                  val value: String,
                  val namespace: String,
                  val prefix: String) {
        override fun toString(): String {
            return "$prefix:$name=\"$value\""
        }
    }

    class XmlNode(val name: String) {
        val children = mutableListOf<XmlNode>()
        val attrs = mutableMapOf<String, XmlAttr>()

        override fun toString(): String {
            return with(StringBuilder()) {
                append('<').append(name)
                attrs.forEach { append("\n    ").append(it.value) }
                append(">\n")
                children.forEach {
                    append("    ")
                    append(it.toString().lines().joinToString(separator = "\n    "))
                    append('\n')
                }
                append("</").append(name).append('>')
                toString()
            }
        }
    }

    private fun layout(f: File) = with(xmlPullParser) {
        log("layout($f)")
        setInput(f.reader())
        val stack = Stack<XmlNode>()
        var last: XmlNode? = null
        try {
            read@ while (true) {
                when (next()) {
                    START_DOCUMENT -> {}
                    START_TAG -> {
                        val node = XmlNode(name)
                        for (i in 0 until attributeCount) {
                            node.attrs.put(getAttributeName(i), XmlAttr(
                                    name = getAttributeName(i),
                                    value = getAttributeValue(i),
                                    namespace = getAttributeNamespace(i),
                                    prefix = getAttributePrefix(i)
                            ))
                        }
                        if (!stack.empty()) {
                            stack.peek().children.add(node)
                        }
                        stack.push(node)
                    }
                    END_TAG -> {
                        last = stack.pop()
                    }
                    END_DOCUMENT -> break@read
                }
            }
        } catch (e: XmlPullParserException) {
            err("failure in parse xml file: $this", e)
        }
        return@with last
    }

    private fun log(msg: String) = println("[TestPlugin] $msg")

    private fun err(msg: String, t: Throwable? = null) {
        System.err.println("[TestPlugin] $msg")
        t?.printStackTrace(System.err)
    }

}