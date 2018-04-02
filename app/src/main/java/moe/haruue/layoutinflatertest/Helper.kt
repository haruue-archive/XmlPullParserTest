package moe.haruue.layoutinflatertest

import android.content.Context
import moe.haruue.annotation.ViewCreator

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
@ViewCreator
fun createCustomView(context: Context): CustomView {
    return CustomView(context)
}