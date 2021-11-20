package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.SpannableStringBuilder
import com.venson.versatile.ubb.style.AbstractStyle

open class UBBSpannableStringConvert : AbstractConvert() {

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

    override fun onStyleParsed(style: AbstractStyle, start: Int, end: Int, align: Paint.Align) {
        mSpannableString.append(style.getSpanText())
        style.getSpan()?.let { span ->
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