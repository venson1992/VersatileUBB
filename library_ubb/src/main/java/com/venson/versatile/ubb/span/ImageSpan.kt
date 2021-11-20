package com.venson.versatile.ubb.span

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import java.lang.ref.WeakReference

/**
 * 图片自定义Span
 */
class ImageSpan : ImageSpan, ISpan {

    private var mDrawableRef: WeakReference<Drawable?>? = null

    private val mImageStyle: ImageStyle

    constructor(context: Context, bitmap: Bitmap, imageStyle: ImageStyle) : this(
        context,
        bitmap,
        imageStyle,
        DynamicDrawableSpan.ALIGN_BASELINE
    )

    constructor(
        context: Context,
        bitmap: Bitmap,
        imageStyle: ImageStyle,
        verticalAlignment: Int
    ) : super(
        context,
        bitmap,
        verticalAlignment
    ) {
        mImageStyle = imageStyle
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        getCachedDrawable()?.let { cache ->
            canvas.save()
            val transY = y - cache.bounds.bottom
            canvas.translate(x, transY.toFloat())
            cache.draw(canvas)
            canvas.restore()
        }
    }

    private fun getCachedDrawable(): Drawable? {
        val wr = mDrawableRef
        var d: Drawable? = null
        if (wr != null) d = wr.get()
        if (d == null) {
            d = drawable
            mDrawableRef = WeakReference(d)
        }
        return d
    }

    override fun getStyle(): AbstractStyle {
        return mImageStyle
    }

    /**
     * 该样式前置是否另起一行
     */
    override fun isStartSingleLine(): Boolean = true

    /**
     * 该样式后置是否另起一行
     */
    override fun isEndSingleLine(): Boolean = true
}