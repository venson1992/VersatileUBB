package com.aiwu.market.ubb.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.LineBackgroundSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.span.SpanContent

/**
 * 可换行的自定义span
 * 背景支持圆角设置
 * 前景支持颜色设置
 */
abstract class BaseLineEnableSpan(
    private val spanContent: SpanContent,
    private val align: Paint.Align = Paint.Align.LEFT,
    @ColorInt val textColor: Int = Color.TRANSPARENT,
    @ColorInt val backgroundColor: Int = Color.TRANSPARENT,
    @Px val radius: Float = 0F
) : CharacterStyle(), LineBackgroundSpan, ISpan {

    override fun getSpanContent(): SpanContent {
        return spanContent
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val fontMetrics = paint.fontMetrics
        /*
        计算背景上下区域
         */
        val rectBottom = baseline + fontMetrics.bottom
        val rectTop = baseline + fontMetrics.top
        /*
        计算左右区域
         */
        val spannedString = SpannableString(text)
        val spanStart = spannedString.getSpanStart(this)
        val spanEnd = spannedString.getSpanEnd(this)
        val lineWidth = paint.measureText(text, start, end)
        var rectLeft: Float
        var rectRight: Float
        if (lineWidth < right - left) {
            rectLeft = when (align) {
                Paint.Align.LEFT -> {
                    left.toFloat()
                }
                Paint.Align.RIGHT -> {
                    right - lineWidth
                }
                else -> {
                    (right - left - lineWidth).div(2) + left
                }
            }
            rectRight = rectLeft + lineWidth
        } else {
            rectLeft = left.toFloat()
            rectRight = right.toFloat()
        }
        if (spanStart > start) {
            val offset = (lineWidth * (spanStart - start)).div(end - start)
            rectLeft += offset
        }
        if (spanEnd < end) {
            val offset = (lineWidth * (end - spanEnd)).div(end - start)
            rectRight -= offset
        }
        val rectF = RectF(rectLeft, rectTop, rectRight, rectBottom)
        val originColor = paint.color
        paint.color = backgroundColor
        canvas.drawRoundRect(rectF, radius, radius, paint)
        paint.color = originColor
    }

    override fun updateDrawState(tp: TextPaint?) {
        if (textColor != Color.TRANSPARENT) {
            tp?.color = textColor
        }
        tp?.isUnderlineText = false
        tp?.linkColor = textColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tp?.underlineColor = Color.TRANSPARENT
        }
    }

    /**
     * 获取当前样式的ubb代码
     */
    override fun getUBB(): String {
        return spanContent.toUBB(getTag())
    }

    /**
     * 获取当前样式的文本内容
     */
    override fun getText(): String {
        return spanContent.getText()
    }

    /**
     * 该样式前置是否另起一行
     */
    override fun isStartSingleLine(): Boolean = false

    /**
     * 该样式后置是否另起一行
     */
    override fun isEndSingleLine(): Boolean = false
}