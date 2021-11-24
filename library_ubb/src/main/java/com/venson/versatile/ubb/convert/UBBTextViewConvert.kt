package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.Spanned
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.widget.UBBTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class UBBTextViewConvert(private val textView: UBBTextView) : AbstractConvert() {

    override fun resetContent() {
        textView.setSpannableText("")
    }

    override fun onHTMLSpannedParsed(htmlSpanned: Spanned, start: Int, end: Int) {
        textView.appendText(htmlSpanned)
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        content?.let {
            textView.appendText(content)
            textView.setSpan(span, start, end, align)
        }
    }

    override fun onStyleParsed(style: AbstractStyle, start: Int, end: Int, align: Paint.Align) {
        if (style is ImageStyle) {
            textView.appendText(style.getSpanText())
            GlobalScope.launch(Dispatchers.IO) {
                val imageSpan = style.getImageSpan(
                    textView.context,
                    textView.measuredWidth - textView.paddingStart - textView.paddingEnd
                )
                withContext(Dispatchers.Main) {
                    UBB.logV("launch", "imageSpan=$imageSpan;start=$start;end=$end")
                    textView.setSpan(imageSpan, start, end, align)
                }
            }
            return
        }
        textView.appendText(style.getSpanText())
        textView.setSpan(style.getSpan(), start, end, align)
    }

    override fun getContentLength(): Int {
        return textView.length()
    }
}