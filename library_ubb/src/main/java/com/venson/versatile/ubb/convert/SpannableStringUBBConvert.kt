package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.SpannableStringBuilder

open class SpannableStringUBBConvert : UBBConvert() {

    private val mSpannableString: SpannableStringBuilder by lazy {
        SpannableStringBuilder()
    }

    fun getSpannableString(): CharSequence = mSpannableString

    override fun resetContent() {
        mSpannableString.clear()
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        if (!content.isNullOrEmpty()) {
            mSpannableString.append(content)
        }
        span?.let {
            mSpannableString.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun getContentLength(): Int {
        return mSpannableString.length
    }
}