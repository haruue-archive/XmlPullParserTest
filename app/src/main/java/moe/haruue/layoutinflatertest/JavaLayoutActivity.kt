package moe.haruue.layoutinflatertest

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import moe.haruue.layoutinflatertest.layout.ActivityJavaLayout

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class JavaLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = ActivityJavaLayout.setContentView(this)

        layout.text1.text = "hello"

    }

    fun buildLayout(context: Context): View {

        val root = LinearLayout(context)
        root.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        root.orientation = LinearLayout.VERTICAL

        val text1 = TextView(context)
        text1.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.START
        }
        text1.text = "text1"
        root.addView(text1)

        val text2 = TextView(context)
        text2.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        text2.text = "text2"
        root.addView(text2)

        val text3 = TextView(context)
        text3.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.END
        }
        text3.text = "text3"
        root.addView(text3)

        return root
    }

}