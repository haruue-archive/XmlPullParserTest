package moe.haruue.gradle.plugin.test

import com.android.build.gradle.AppExtension
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.xmlpull.mxp1.MXParserFactory
import org.xmlpull.v1.XmlPullParser.*
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.util.*
import javax.lang.model.element.Modifier

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
        log("project.buildDir: ${project.buildDir}")
        log("project.buildFile: ${project.buildFile}")
        project.extensions.findByType(AppExtension::class.java).run {
            this ?: return@run

            val aptOutputRoot = File(project.buildDir.absolutePath, "generated/source/apt")
            log("aptOutputRoot: ${aptOutputRoot.absolutePath}")
            applicationVariants.all {
                log("applicationVariant.dirName: ${it.dirName}")
                val aptOutputDir = File(aptOutputRoot, it.dirName)
                aptOutputDir.mkdirs()
                log("aptOutputDir: ${aptOutputDir.absolutePath}")

//                writeJavaFile(aptOutputDir)
/*

                layouts.forEach {
                    xmlPullParser.setInput(it.reader())
                    LayoutParser("moe.haruue.layoutinflatertest",
                            it.name.removeSuffix(".xml"),
                            xmlPullParser).toJavaFile().writeTo(aptOutputDir)
                }
*/

                log("applicationVariant.applicationId: ${it.applicationId}")
                log("packageName == applicationVariant.generateBuildConfig.buildConfigPackageName: ${it.generateBuildConfig.buildConfigPackageName}")
                log("rootDir == project.rootDir: ${project.rootDir}")
                log("projectDir == project.projectDir : ${project.projectDir}")
                log("buildDir == project.buildDir: ${project.buildDir}")
                log("resDir == sourceSets.map...: ${sourceSets.map { it.res }.flatMap { it.srcDirs }}")
                log("all res == sourceSets.map...: ${sourceSets.map { it.res }.flatMap { it.sourceFiles }}")
                log("layout == sourceSets.map...: ${sourceSets.map { it.res }.flatMap { it.sourceFiles }.filter { it.isLayout() }}")

                val rootDir = project.rootDir
                val projectDir = project.projectDir
                val packageName = it.generateBuildConfig.buildConfigPackageName
                val buildDir = project.buildDir
                val resDirs = sourceSets.map { it.res }.flatMap { it.srcDirs }
                val layouts = sourceSets.map { it.res }.flatMap { it.sourceFiles }.filter { it.isLayout() }

                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.rootDir"] = rootDir.absolutePath
                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.projectDir"] = projectDir.absolutePath
                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.packageName"] = packageName
                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.buildDir"] = buildDir.absolutePath
                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.resDirs"] = resDirs.joinToString(separator = ",")
                it.javaCompileOptions.annotationProcessorOptions.arguments["layoutinflater.layouts"] = resDirs.joinToString(separator = ",")

            }
        }
    }

    val T = "${'$'}T"
    val S = "${'$'}S"

    private fun writeJavaFile(dst: File) {
        val main = MethodSpec.methodBuilder("main").apply {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            returns(Void.TYPE)
            addParameter(Array<String>::class.java, "args")
            addStatement("$T.out.println($S)", System::class.java, "Hello, JavaPoet")
        }.build()

        val hello = TypeSpec.classBuilder("HelloWorld").apply {
            addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            addMethod(main)
        }.build()

        val javaFile = JavaFile.builder("moe.haruue.generated", hello).apply {

        }.build()

        javaFile.writeTo(dst)
    }

    private fun File.isLayout(): Boolean {
        return extension == "xml" &&
                parentFile.name.matches(Regex("^layout(-[A-Za-z0-9_]+)*"))
/*        xmlPullParser.setInput(reader())
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
        return result*/
    }

    class XmlAttr(val name: String,
                  val value: String,
                  val namespace: String,
                  val prefix: String,
                  val type: String) {
        override fun toString(): String {
            return "$prefix:$name=\"$value\" (type=$type)"
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
                                    prefix = getAttributePrefix(i),
                                    type = getAttributeType(i)
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