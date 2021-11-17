package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.venson.versatile.ubb.fix.TouchSpanFixTextView
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.span.TouchableSpan

/**
 * 支持UBB的TextView
 */
class UBBTextView : TouchSpanFixTextView {

    private var mText: SpannableStringBuilder = SpannableStringBuilder()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, android.R.attr.textViewStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    /**
     * 插入span
     */
    fun insertSpan(span: Any?, start: Int, end: Int, content: String?, align: Paint.Align) {
        if (span != null && span is ISpan) {
            insertSpan(span, align)
            return
        }
        if (!content.isNullOrEmpty()) {
            mText.append(content)
        }
        if (span != null) {
            mText.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        setSpannableText(mText)
    }

    /**
     * 插入span样式
     */
    fun insertSpan(span: ISpan, align: Paint.Align) {
        val start: Int = mText.length
        mText.append(span.getText())
        val end: Int = mText.length
        span.getClickListener()?.let {
            setMovementMethodDefault()
            mText.setSpan(
                object : TouchableSpan(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
                ) {
                    override fun onSpanClick(widget: View?) {
                        Log.d("test", "点击了span")
                        it.onClick(this@UBBTextView)
                    }
                },
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        mText.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpannableText(mText)
    }

    fun setSpannableText(text: CharSequence?) {
        setText(text, BufferType.SPANNABLE)
    }

    fun getSpannableText(): CharSequence {
        return mText
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text == mText) {
            super.setText(text, type)
        }
        mText = if (text is SpannableStringBuilder) {
            text
        } else {
            SpannableStringBuilder(text)
        }
        super.setText(mText, type)
    }
}