package moe.haruue.layoutparser.compiler.tools

import com.squareup.javapoet.ClassName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
fun String.parsePackageName(): String {
    var packageName = this
    val dot = packageName.lastIndexOf(".")
    if (dot > 0) {
        packageName = packageName.substring(0, dot)
    } else {
        packageName = ""
    }
    return packageName
}

fun String.parseSimpleClassName(): String {
    var simpleName = this
    val dot = simpleName.lastIndexOf(".")
    if (dot > 0) {
        simpleName = simpleName.substring(dot + 1)
    }
    return simpleName
}

fun String.parseClassName() = ClassName.get(parsePackageName(), parseSimpleClassName())

fun String.underlineToUpperCamel(): String {
    val words = split("_")
    return words.joinToString(separator = "") { it.upperCaseFirst() }
}

fun String.upperCaseFirst(): String {
    val sb = StringBuilder()
    if (length > 0) {
        sb.append(substring(0, 1).toUpperCase())
        if (length > 1) {
            sb.append(substring(1))
        }
    }
    return sb.toString()
}
