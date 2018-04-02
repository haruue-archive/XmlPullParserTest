package moe.haruue.layoutparser.compiler

import com.google.auto.service.AutoService
import moe.haruue.annotation.LayoutAdapter
import moe.haruue.annotation.LayoutBuildInfo
import moe.haruue.annotation.ViewCreator
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class LayoutProcessor : AbstractProcessor() {



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


        log(">>>>> process() end")
        return false
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
                LayoutAdapter::class.java.name,
                ViewCreator::class.java.name,
                LayoutBuildInfo::class.java.name
        )
    }

    fun log(f: String, vararg args: Any) {
        processingEnv.messager.printMessage(Diagnostic.Kind.OTHER, "[LayoutProcessor] %s".format(f.format(*args)))
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

}
