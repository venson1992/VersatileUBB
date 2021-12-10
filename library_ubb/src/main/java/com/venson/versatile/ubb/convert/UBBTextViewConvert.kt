package com.venson.versatile.ubb.convert

import android.widget.TextView
import com.venson.versatile.ubb.span.GlideImageSpan
import com.venson.versatile.ubb.style.AbstractStyle

/**
 * 针对TextView的UBB解析器
 */
open class UBBTextViewConvert(private val textView: TextView) :
    AbstractConvert(textView.context) {

    override fun isOnlyText(): Boolean {
        return false
    }

    override fun onParseStart() {
        textView.text = ""
    }

    override fun onParseComplete() {
        textView.text = getSpannableString()
    }

    override fun getImageLoadSuccessListener(): GlideImageSpan.OnImageLoadSuccessListener? {
        return object : GlideImageSpan.OnImageLoadSuccessListener {
            override fun onLoadSuccess() {
                onParseComplete()
            }
        }
    }

    override suspend fun onInsertStyle(customStyle: AbstractStyle) {
        super.onInsertStyle(customStyle)
    }
}