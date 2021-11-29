package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
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

    fun appendText(charSequence: CharSequence) {
        mText.append(charSequence)
        setSpannableText(mText)
    }

    fun setSpan(span: Any?, start: Int, end: Int, align: Paint.Align) {
        span ?: return
        if (span is ISpan) {
            span.getClickListener()?.let {
                setMovementMethodDefault()
                try {
                    mText.setSpan(
                        object : TouchableSpan(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                            Color.TRANSPARENT
                        ) {
                            override fun onSpanClick(widget: View?) {
                                it.onClick(this@UBBTextView)
                            }
                        },
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            mText.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setSpannableText(mText)
    }

    fun setSpannableText(text: CharSequence?) {
        setText(text, BufferType.SPANNABLE)
    }

    fun getSpannableText(): CharSequence {
        return mText
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        val currentString = try {
            mText.toString()
        } catch (e: Exception) {
            null
        }
        if (text?.toString() ?: "" == currentString) {
            super.setText(text, type)
            return
        }
        mText = when (text) {
            null -> {
                SpannableStringBuilder("")
            }
            is SpannableStringBuilder -> {
                text
            }
            else -> {
                SpannableStringBuilder(text)
            }
        }
        super.setText(mText, type)
    }
}