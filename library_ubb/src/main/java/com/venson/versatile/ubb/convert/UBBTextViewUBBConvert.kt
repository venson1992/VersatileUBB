package com.venson.versatile.ubb.convert

import android.graphics.Paint
import com.venson.versatile.ubb.widget.UBBTextView

open class UBBTextViewUBBConvert(private val textView: UBBTextView) : UBBConvert() {

    override fun resetContent() {
        textView.setSpannableText("")
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        textView.insertSpan(span, start, end, content, align)
    }

    override fun getContentLength(): Int {
        return textView.length()
    }
}