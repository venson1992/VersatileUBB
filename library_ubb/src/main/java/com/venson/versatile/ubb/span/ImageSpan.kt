package com.venson.versatile.ubb.span

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import com.venson.versatile.ubb.UBB
import java.lang.ref.WeakReference

/**
 * 图片自定义Span
 */
class ImageSpan : ImageSpan, ISpan {

    //Uri.toString(content://),http://,file://,drawable://
    private var mSpanContent = SpanContent()

    private var mDrawableRef: WeakReference<Drawable?>? = null

    constructor(context: Context, bitmap: Bitmap, path: String) : this(
        context,
        bitmap,
        path,
        DynamicDrawableSpan.ALIGN_BASELINE
    )

    constructor(context: Context, bitmap: Bitmap, path: String, verticalAlignment: Int) : super(
        context,
        bitmap,
        verticalAlignment
    ) {
        mSpanContent.setText(path)
    }

    constructor(drawable: Drawable, source: String) : this(
        drawable,
        source,
        DynamicDrawableSpan.ALIGN_BASELINE
    )

    constructor(drawable: Drawable, source: String, verticalAlignment: Int) : super(
        drawable,
        source,
        verticalAlignment
    ) {
        mSpanContent.setText(source)
    }

    constructor(context: Context, uri: Uri) : this(
        context,
        uri,
        DynamicDrawableSpan.ALIGN_BASELINE
    )

    constructor(context: Context, uri: Uri, verticalAlignment: Int) : super(
        context,
        uri,
        verticalAlignment
    ) {
        mSpanContent.setText(uri.toString())
    }

    constructor(context: Context, resourceId: Int) : this(
        context,
        resourceId,
        DynamicDrawableSpan.ALIGN_BASELINE
    )

    constructor(context: Context, resourceId: Int, verticalAlignment: Int) : super(
        context,
        resourceId,
        verticalAlignment
    ) {
        val resourceName = context.resources.getResourceName(resourceId)
        mSpanContent.setText("${UBB.SCHEME_DRAWABLE}$resourceName")
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

    override fun getTag(): String = "img"

    override fun getSpanContent(): SpanContent {
        return mSpanContent
    }

    /**
     * 获取当前样式的ubb代码
     */
    override fun getUBB(): String {
        return mSpanContent.toUBB(getTag())
    }

    /**
     * 获取当前样式的文本内容
     */
    override fun getText(): String {
        return mSpanContent.getText()
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