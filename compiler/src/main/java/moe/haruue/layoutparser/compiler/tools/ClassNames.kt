package moe.haruue.layoutparser.compiler.tools

import com.squareup.javapoet.ClassName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
fun String.toClass(): Class<*> = Class.forName(this)
fun ClassName.toClass(): Class<*> = reflectionName().toClass()
fun Class<*>.toClassName(): ClassName = ClassName.get(this)
fun String.toClassName(): ClassName = toClass().toClassName()

val cnContext = ClassName.get("android.content", "Context")
val cnActivity = ClassName.get("android.app", "Activity")
val cAttributeSet = Class.forName("android.util.AttributeSet")


