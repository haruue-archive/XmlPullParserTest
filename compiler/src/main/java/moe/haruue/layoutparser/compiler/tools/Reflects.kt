package moe.haruue.layoutparser.compiler.tools

import java.lang.reflect.Method

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
fun Class<*>.getMethodRecursive(name: String, vararg parameterTypes: Class<*>): Method {
    var clazz: Class<*>? = this
    while (clazz != null) {
        try {
            return getMethod(name, *parameterTypes)
        } catch (e: NoSuchMethodException) {
            clazz = superclass
        }
    }
    throw NoSuchMethodException(
            "no such " +
                    "method $name(${parameterTypes.joinToString(separator = ",")}) " +
                    "in ${this.name}")
}
