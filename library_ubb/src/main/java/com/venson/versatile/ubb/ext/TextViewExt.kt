package com.venson.versatile.ubb.ext

import android.widget.TextView
import com.venson.versatile.ubb.convert.AbstractConvert
import com.venson.versatile.ubb.convert.UBBSimpleConvert
import com.venson.versatile.ubb.convert.UBBTextViewConvert

/**
 * 为textview显示ubb样式
 */
fun TextView?.fromUBB(ubb: String?, ubbConvert: UBBTextViewConvert? = null) {
    this ?: return
    if (ubb.isNullOrEmpty()) {
        text = ""
        return
    }
    val spannableStringUBBConvert = ubbConvert ?: UBBTextViewConvert(this)
    spannableStringUBBConvert.parseUBB(ubb)
}

/**
 * 为textview显示ubb样式
 */
fun TextView?.fromUBBWithOnlyText(ubb: String?, ubbConvert: AbstractConvert? = null) {
    this ?: return
    if (ubb.isNullOrEmpty()) {
        text = ""
        return
    }
    val spannableStringUBBConvert = ubbConvert ?: UBBSimpleConvert(context)
    spannableStringUBBConvert.parseUBB(ubb)
    text = spannableStringUBBConvert.getContent()
}