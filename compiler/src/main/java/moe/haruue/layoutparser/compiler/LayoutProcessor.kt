package moe.haruue.layoutparser.compiler

import com.google.auto.service.AutoService
import moe.haruue.annotation.LayoutAdapter
import moe.haruue.annotation.ViewCreator
import org.xmlpull.mxp1.MXParserFactory
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedOptions(
        "layoutinflater.rootDir",
        "layoutinflater.projectDir",
        "layoutinflater.packageName",
        "layoutinflater.buildDir",
        "layoutinflater.resDirs",
        "layoutinflater.layouts"
)
class LayoutProcessor : AbstractProcessor() {

    lateinit var rootDir: File
    lateinit var projectDir: File
    lateinit var packageName: String
    lateinit var buildDir: File
    lateinit var resDirs: List<File>
    lateinit var layouts: List<File>
    var complete = false


    val xmlPullParser by lazy {
        val factory = MXParserFactory.newInstance()
        with(factory) {
            isNamespaceAware = true
        }
        factory.newPullParser()
    }


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        rootDir = File(processingEnv.options["layoutinflater.rootDir"])
        projectDir = File(processingEnv.options["layoutinflater.projectDir"])
        packageName = processingEnv.options["layoutinflater.packageName"]!!
        buildDir = File(processingEnv.options["layoutinflater.buildDir"])
        resDirs = processingEnv.options["layoutinflater.resDirs"]!!.split(",").map { File(it) }
        layouts = processingEnv.options["layoutinflater.layouts"]!!.split(",").map { File(it) }

        log("LayoutProcessor(rootDir=$rootDir, projectDir=$projectDir, packageName=$packageName, buildDir=$buildDir, resDirs=$resDirs, layouts=$layouts)")
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        log("<<<<< process() start, annotation=%s", annotations)



        val elements = roundEnv.getElementsAnnotatedWith(ViewCreator::class.java)
        for (e in elements) {
            val annotation = e.getAnnotation(ViewCreator::class.java)
            log("e.simpleName: %s", e.simpleName)
            log("e.enclosingElement: %s", e.enclosingElement)
            log("e.modifiers: %s", e.modifiers)
            if (e.kind == ElementKind.METHOD) {
                e as ExecutableElement
                log("e.returnType: %s", e.returnType)
                log("method: fun %s.%s(%s): %s",
                        e.enclosingElement,
                        e.simpleName,
                        e.parameters.joinToString(separator = ",") {
                            "${it.simpleName}: ${it.asType()}"
                        },
                        e.returnType)
            }
        }

        if (!complete) {
            log("===== processing layout files =====")
            val xml = xmlPullParser
            for (layout in layouts) {
                log("layout: $layout")
                xml.setInput(layout.reader())
                LayoutParser(packageName, layout.nameWithoutExtension, xml)
                        .toJavaFile().writeTo(processingEnv.filer)
            }
            complete = true
            log("===== processed layout files =====")
        }

        log(">>>>> process() end")
        return false
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
                LayoutAdapter::class.java.name,
                ViewCreator::class.java.name,
                "*"
        )
    }

    fun log(f: String, vararg args: Any) {
        processingEnv.messager.printMessage(Diagnostic.Kind.OTHER, "[LayoutProcessor] %s".format(f.format(*args)))
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

}
