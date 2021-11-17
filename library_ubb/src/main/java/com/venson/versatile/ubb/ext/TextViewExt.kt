package com.venson.versatile.ubb.ext

import android.widget.TextView
import com.venson.versatile.ubb.convert.SpannableStringUBBConvert

/**
 * 为textview显示ubb样式
 */
fun TextView?.fromUBB(ubb: String?, ubbConvert: SpannableStringUBBConvert? = null) {
    this ?: return
    if (ubb.isNullOrEmpty()) {
        text = ""
        return
    }
    val spannableStringUBBConvert = ubbConvert ?: SpannableStringUBBConvert()
    spannableStringUBBConvert.parseUBB(ubb)
    text = spannableStringUBBConvert.getSpannableString()
}