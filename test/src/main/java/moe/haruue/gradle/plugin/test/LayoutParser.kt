package moe.haruue.gradle.plugin.test

import com.squareup.javapoet.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.*
import java.util.*
import javax.lang.model.element.Modifier

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LayoutParser(
        val packageName: String,
        val layoutName: String,
        val xml: XmlPullParser
) {

    companion object {
        // fuck kotlin string format
        val S = "${'$'}S"
        val T = "${'$'}T"
        val N = "${'$'}N"
        val PREFIX = "__view_"
        val PREFIX_NOID = "${PREFIX}noid_"
        val ROOT_VIEW_NAME = "${PREFIX}root"
        fun paramNameOf(fieldName: String) = "${PREFIX}param$fieldName"
        val CONTEXT_CLASS_NAME = ClassName.get("android.content", "Context")
        val ACTIVITY_CLASSNAME = ClassName.get("android.app", "Activity")
    }

    object DimensionUnit {
        val NO_UNIT = ""
        val PX = "px"
        val DP = "dp"
        val SP = "sp"
        val EM = "em"
    }

    lateinit var constructorBuilder: MethodSpec.Builder
    val methods = mutableListOf<MethodSpec>()
    val fields = mutableListOf<FieldSpec>()

    private data class ViewType(val id: String, val type: String)
    private data class Dimension(val value: Int, val unit: String)
    private val stack = Stack<ViewType>()
    private var currentNoId = 0
    private val generatePackage = "$packageName.layout"
    private val generateSimpleClassName = "${layoutName.underlineToUpperCamel()}Layout"
    private val generateClassName = ClassName.get(generatePackage, generateSimpleClassName)

    fun toJavaFile(): JavaFile {
        val generateClass = TypeSpec.classBuilder(generateClassName).apply {
            addModifiers(Modifier.FINAL, Modifier.PUBLIC)
            addMethod(constructorBuilder.build())
            addFields(fields)
            addMethods(methods)
        }.build()

        return JavaFile.builder("$packageName.layout", generateClass).apply {
            addFileComment("generated java layout of $layoutName")
        }.build()
    }

    init {
        initToolsMethods()
        constructorBuilder = MethodSpec.constructorBuilder().apply {
            addModifiers(Modifier.PRIVATE)
            addParameter(CONTEXT_CLASS_NAME, "context")
        }
        with(xml) {
            read@ while (true) {
                when (next()) {
                    START_DOCUMENT -> {}
                    START_TAG -> doStartTag(xml)
                    END_TAG -> doEndTag(xml)
                    END_DOCUMENT -> break@read
                }
            }
        }

    }

    private fun doStartTag(xml: XmlPullParser) {
        val isRoot = stack.empty()
        val parent = if (isRoot) {
            ViewType("INVALID", "android.view.ViewGroup")
        } else {
            stack.peek()
        }
        val attrs = xml.parseAttrs()

        // field type (view type)
        val type = parseViewClass(xml.name)

        // field name && set id or not
        var field = attrs["android:id"]
        val hasId = field != null
        if (field == null) {
            field = "$PREFIX_NOID${currentNoId++}"
        } else {
            field = field.split("/")[1]
        }

        // width && height
        val width = attrs["android:layout_width"]!!.parseDimension()
        val height = attrs["android:layout_height"]!!.parseDimension()

        generateJavaCodeForTag(field, type, hasId, attrs, width, height, isRoot, parent)

        val node = ViewType(field, type)

        if (isRoot) {
            setRoot(node)
        }

        // do this in end of this method
        stack.push(node)
    }

    private fun generateJavaCodeForTag(name: String, type: String, hasId: Boolean, attrs: Map<String, String>,
                                       width: Dimension, height: Dimension, isRoot: Boolean, parent: ViewType) {
        val typeClassName = type.parseClassName()
        val field = FieldSpec.builder(typeClassName, name,
                if (hasId) Modifier.PUBLIC else Modifier.PRIVATE,
                Modifier.FINAL).build()
        fields.add(field)

        constructorBuilder.addComment("===== init $name: $type =====")

        val parentClassName = parent.type.parseClassName()
        val layoutParamsName = paramNameOf(name)

        constructorBuilder.addCode("""
            this.$name = new $T(context);
            $T.LayoutParams $layoutParamsName = new $T.LayoutParams(${width.value}, ${height.value});
        """.trimIndent(), typeClassName,
                parentClassName, parentClassName)

        // parse other attrs here...

        constructorBuilder.addCode("""
            this.$name.setLayoutParams($layoutParamsName);
        """.trimIndent())

        if (!isRoot) {
            constructorBuilder.addCode("""
                this.${parent.id}.addView(this.$name);
            """.trimIndent())
        }

        constructorBuilder.addComment("===== end $name: $type =====")
    }

    private fun setRoot(node: ViewType) {
        val rootViewClassName = node.type.parseClassName()
        val root = FieldSpec.builder(rootViewClassName, ROOT_VIEW_NAME,
                Modifier.PRIVATE, Modifier.FINAL).build()
        fields.add(root)

        constructorBuilder.addCode("""
            this.$ROOT_VIEW_NAME = this.${node.id};
        """.trimIndent())

        val getRootView = MethodSpec.methodBuilder("getRootView").apply {
            addModifiers(Modifier.PUBLIC)
            returns(rootViewClassName)
            addCode("""
                return $ROOT_VIEW_NAME;
            """.trimIndent())
        }.build()
        methods.add(getRootView)
    }

    private fun doEndTag(xml: XmlPullParser) {
        stack.pop()
    }

    private fun String.parseDimension() = when (this) {
        "match_parent" -> Dimension(-1, DimensionUnit.NO_UNIT)
        "wrap_content" -> Dimension(-2, DimensionUnit.NO_UNIT)
        else -> {
            Dimension(findNumber(), findNonNumber())
        }
    }

    private fun XmlPullParser.parseAttrs(): Map<String, String> {
        val attrs = mutableMapOf<String, String>()
        for (i in 0 until attributeCount) {
            attrs["${getAttributePrefix(i)}:${getAttributeName(i)}"] = getAttributeValue(i)
        }
        return attrs
    }

    private fun initToolsMethods() {
        val create = MethodSpec.methodBuilder("create").apply {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            addParameter(CONTEXT_CLASS_NAME, "context")
            returns(generateClassName)
            addCode("""
                return new $T(context);
            """.trimIndent(), generateClassName)
        }.build()
        methods.add(create)
        val setContentView = MethodSpec.methodBuilder("setContentView").apply {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            addParameter(ACTIVITY_CLASSNAME, "activity")
            returns(generateClassName)
            addCode("""
                $T layout = $T.create(activity);
                activity.setContentView(layout.$ROOT_VIEW_NAME);
                return layout;
            """.trimIndent(), generateClassName, generateClassName)
        }.build()
        methods.add(setContentView)
    }

    private fun parseViewClass(tagName: String) = when (tagName) {
        "LinearLayout" -> "android.widget.LinearLayout"
        "TextView" -> "android.widget.TextView"
        else -> tagName
    }

    private fun String.findNumber() = split("\\D").joinToString(separator = "").toInt()
    private fun String.findNonNumber() = split("\\d").joinToString(separator = "")
    private fun String.parsePackageName(): String {
        var packageName = this
        val dot = packageName.lastIndexOf(".")
        if (dot > 0) {
            packageName = packageName.substring(0, dot)
        } else {
            packageName = ""
        }
        return packageName
    }
    private fun String.parseSimpleClassName(): String {
        var simpleName = this
        val dot = simpleName.lastIndexOf(".")
        if (dot > 0) {
            simpleName = simpleName.substring(dot + 1)
        }
        return simpleName
    }
    private fun String.parseClassName() = ClassName.get(parsePackageName(), parseSimpleClassName())
    private fun String.underlineToUpperCamel(): String {
        val words = split("_")
        return words.joinToString(separator = "") { it.upperCaseFirst() }
    }
    private fun String.upperCaseFirst(): String {
        val sb = StringBuilder()
        if (length > 0) {
            sb.append(substring(0, 1).toUpperCase())
            if (length > 1) {
                sb.append(substring(1))
            }
        }
        return sb.toString()
    }
}