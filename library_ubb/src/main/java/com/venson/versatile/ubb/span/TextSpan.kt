package com.venson.versatile.ubb.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 文本型span
 */
abstract class TextSpan(spanContent: SpanContent) : BaseSpan(spanContent) {

    //背景画笔
    private var mPaint: Paint = Paint()

    //取自textview大小
    @Px
    private var mSourceTextSize = 0

    //缩放后大小
    @Px
    private var mTextSize = 0

    //缩放的大小
    @Px
    private var mDifferTextSize = 0

    //缩放后文本内容长度
    @Px
    private var mTextMeasureWidth = 0

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        updateTextSize(paint)
        paint.textSize = mTextSize.toFloat()
        mTextMeasureWidth = paint.measureText(text, start, end).toInt()
        mTextMeasureWidth += getPadding()
        return mTextMeasureWidth + getMarginStart() + getMarginEnd()
    }

    /**
     * 缩放显示文本大小
     */
    private fun updateTextSize(paint: Paint) {
        if (mTextSize == 0) {
            mSourceTextSize = paint.textSize.toInt()
            mTextSize = (mSourceTextSize * getTextScale()).roundToInt()
            mDifferTextSize = mSourceTextSize - mTextSize
        }
    }

    override fun draw(
        canvas: Canvas,
        charSequence: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        updateTextSize(paint)
        charSequence ?: return
        /*
        获取显示的文本以及计算其宽度
         */
        val size = canvas.width.div(mTextSize)
        val text: String = if (end - start > size) {
            charSequence.subSequence(start, start + size).toString()
        } else {
            charSequence.subSequence(start, end).toString()
        }
        paint.textSize = mTextSize.toFloat()
        paint.color = getForegroundColor() ?: paint.color
        paint.textAlign = Paint.Align.LEFT
        val textMeasuredWidth = paint.measureText(text)
        val baseLine: Int = y - mDifferTextSize / 2
        Log.d(
            this.javaClass.simpleName,
            "[$text] x = $x;top =$top;baseLine = $baseLine;bottom = $bottom"
        )
        /*
        计算绘制区域
         */
        val rectLeft: Float = x + getMarginStart()
        val rectRight = rectLeft + mTextMeasureWidth
        val rectHeight = mSourceTextSize.toFloat()
        val ascentAbs = abs(paint.ascent())
        val descentAbs = abs(paint.descent())
        val fontHeight = ascentAbs + descentAbs
        var marginVertical = 0f
        try {
            if (rectHeight > fontHeight) {
                marginVertical = (rectHeight - fontHeight) / 2
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val rectTop = baseLine - ascentAbs - marginVertical
        val rectBottom = baseLine + descentAbs + marginVertical
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        Log.d(
            this.javaClass.simpleName,
            "[$text] rectLeft=$rectLeft;rectTop=$rectTop;" +
                    "rectRight=$rectRight;rectBottom=$rectBottom"
        )
        /*
        绘制圆角背景
         */
        mPaint.isAntiAlias = true
        mPaint.color = getBackgroundColor()
        val inDiff = getStrokeWidth().let { strokeWidth ->
            if (strokeWidth > 0) {
                strokeWidth.div(2).toFloat()
            } else {
                0F
            }
        }
        rect.inset(inDiff, inDiff)
        mPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRoundRect(rect, getRadius(), getRadius(), mPaint)
        /*
        绘制边框
         */
        if (inDiff > 0) {
            mPaint.color = getBackgroundColor()
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = inDiff * 2F
            canvas.drawRoundRect(rect, getRadius(), getRadius(), mPaint)
        }
        /*
        绘制文本
         */
        var padding = 0f
        try {
            padding = (rect.width() - textMeasuredWidth) / 2
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        Log.d(this.javaClass.simpleName, "[$text] padding=$padding;baseLine=$baseLine")
        //绘制文字
        canvas.drawText(
            charSequence,
            start,
            end,
            rect.left + padding,
            baseLine.toFloat(),
            paint
        )
    }

    /**
     * 整体与后置的距离，默认0
     */
    @Px
    open fun getMarginStart(): Int {
        return 0
    }

    /**
     * 整体与前置的距离，默认0
     */
    @Px
    open fun getMarginEnd(): Int {
        return 0
    }

    /**
     * 文本与边框的距离，默认0
     */
    @Px
    open fun getPadding(): Int {
        return 0
    }

    /**
     * 背景圆角，默认0
     */
    @Px
    open fun getRadius(): Float {
        return 0F
    }

    /**
     * 背景颜色，默认不设置背景颜色
     */
    @ColorInt
    open fun getBackgroundColor(): Int {
        return Color.TRANSPARENT
    }

    /**
     * 边框颜色
     */
    @ColorInt
    open fun getStrokeColor(): Int {
        return getBackgroundColor()
    }

    /**
     * 边框粗细
     */
    @Px
    open fun getStrokeWidth(): Int {
        return 0
    }

    /**
     * 文本颜色，默认null表示不设置文本颜色，沿用textview
     */
    @ColorInt
    open fun getForegroundColor(): Int? {
        return null
    }

    /**
     * 设置文本缩放，默认不缩放
     */
    @FloatRange(from = 0.0, to = 1.0)
    open fun getTextScale(): Float {
        return 1.0F
    }
}