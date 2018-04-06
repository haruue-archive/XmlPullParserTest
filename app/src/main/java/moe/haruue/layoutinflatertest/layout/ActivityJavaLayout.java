package moe.haruue.layoutinflatertest.layout;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import static android.view.ViewGroup.LayoutParams.*;

/**
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
public class ActivityJavaLayout {

    private ActivityJavaLayout(Context context) {

        // general, we need a stack to store follow datatype
        // data class ViewType(val id: String, val type: String)

        // when START_TAG...
        // parse the view like text1
        // then stack.put("text", "android.widget.TextView")

        // when END_TAG
        // stack.pop(), handle the return value, if stack become empty, it will be $$view$root

        this.$$view$noid$0 = new LinearLayout(context);
        ViewGroup.LayoutParams rootParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.$$view$noid$0.setLayoutParams(rootParam);
        this.$$view$noid$0.setOrientation(LinearLayout.VERTICAL);

        // <TextView
        // try to find android:id, if it has a id, use as field name,
        // or generate a name link $$view$noid$0, use a auto increasing field.
        this.text1 = new TextView(context);
        // if id exist, don't forget to call this.text1.setId(applicationpackagename.R.id.xxxxx)
        // get these two guys from attrs first
        // construct parent layout's LayoutParams Class, get its type from stack.peek().type
        //      android:layout_width="wrap_content" android:layout_height="wrap_content"
        LinearLayout.LayoutParams text1Param = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        text1Param.width = WRAP_CONTENT;
        text1Param.height = WRAP_CONTENT;
        // than ignore the id, layout_width and layout_height, find other thing...
        // begin with layout --> param.xxxxxx
        //      android:layout_gravity="start"
        text1Param.gravity = Gravity.START;
        // else --> view.xxxx
        //      android:text="text1"
        this.text1.setText("text1");
        // >
        // we finally set layout params here so orders is not important.
        this.text1.setLayoutParams(text1Param);
        // add to father view
        // get parent view id by stack.peek().id
        this.$$view$noid$0.addView(this.text1);

        this.text2 = new TextView(context);
        LinearLayout.LayoutParams text2Param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        text2Param.gravity = Gravity.CENTER_HORIZONTAL;
        this.text2.setLayoutParams(text2Param);
        this.text2.setText("text2");
        this.$$view$noid$0.addView(this.text2);

        this.text3 = new TextView(context);
        LinearLayout.LayoutParams text3Param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        text3Param.gravity = Gravity.END;
        this.text3.setLayoutParams(text3Param);
        this.text3.setText("text3");
        this.$$view$noid$0.addView(this.text3);

        this.$$view$root = $$view$noid$0;
    }

    // id as field
    private final LinearLayout $$view$noid$0;
    public final TextView text1;
    public final TextView text2;
    public final TextView text3;
    private final LinearLayout $$view$root;

    public static ActivityJavaLayout create(Context context) {
        return new ActivityJavaLayout(context);
    }

    public static ActivityJavaLayout setContentView(Activity activity) {
        ActivityJavaLayout layout = ActivityJavaLayout.create(activity);
        activity.setContentView(layout.$$view$root);
        return layout;
    }

    public View getRootView() {
        return $$view$root;
    }




}
