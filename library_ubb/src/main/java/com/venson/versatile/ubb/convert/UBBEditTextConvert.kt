package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.widget.UBBEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class UBBEditTextConvert(private val editText: UBBEditText) : AbstractConvert() {

    override fun resetContent() {
        editText.setText("")
    }

    override fun onHTMLSpannedParsed(htmlSpanned: Spanned, start: Int, end: Int) {
        editText.editableText.append(htmlSpanned)
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        content?.let {
            editText.editableText.append(content)
            editText.editableText.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onStyleParsed(style: AbstractStyle, start: Int, end: Int, align: Paint.Align) {
        if (style is ImageStyle) {
            editText.editableText.append(style.getSpanText())
            GlobalScope.launch(Dispatchers.IO) {
                val imageSpan = style.getImageSpan(editText.context, 500)
                withContext(Dispatchers.Main) {
                    UBB.logV("launch", "imageSpan=$imageSpan;start=$start;end=$end")
                    editText.editableText.setSpan(
                        imageSpan,
                        start,
                        end,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            return
        }
        editText.editableText.append(style.getSpanText())
        editText.editableText.setSpan(
            style.getSpan(),
            start,
            end,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun getContentLength(): Int {
        return editText.editableText.length
    }
}