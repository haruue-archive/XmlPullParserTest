package moe.haruue.layoutinflatertest

import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class CustomAttributeSetFactory {
    companion object {

        fun obtainAttributeSet(): AttributeSet {
            return Xml.asAttributeSet(obtainProxyXmlPullParser())
        }

        fun obtainProxyXmlPullParser(): XmlPullParser {
            return Proxy.newProxyInstance(this::class.java.classLoader, arrayOf(XmlPullParser::class.java)) {
                any: Any, method: Method, arrayOfAnys: Array<Any> ->
                Log.d("FXPP", method.toGenericString())
                when (method.returnType) {
                    Int.MAX_VALUE.javaClass -> return@newProxyInstance 10
                    String::class.java -> return@newProxyInstance ""
                }
                return@newProxyInstance method.returnType.getDeclaredConstructor().newInstance()
            } as XmlPullParser
        }

        fun obtainProxyAttributeSet(): AttributeSet {
            return Proxy.newProxyInstance(this::class.java.classLoader, arrayOf(AttributeSet::class.java)) {
                any: Any, method: Method, arrayOfAnys: Array<Any> ->
                Log.d("CAS", method.toGenericString())
                when (method.returnType) {
                    Int.MAX_VALUE.javaClass -> return@newProxyInstance 10
                    String::class.java -> return@newProxyInstance ""
                }
                return@newProxyInstance method.returnType.getDeclaredConstructor().newInstance()
            } as AttributeSet
        }
    }
}

