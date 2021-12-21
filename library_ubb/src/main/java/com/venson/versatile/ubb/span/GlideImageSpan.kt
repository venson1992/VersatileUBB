package com.venson.versatile.ubb.span

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.venson.versatile.ubb.ext.scale
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import java.lang.ref.WeakReference


/**
 * 异步图片样式
 */
class GlideImageSpan : DynamicImageSpan, ISpan {

    private val mImageStyle: ImageStyle

    private var mOnImageLoadedListener: OnImageLoadSuccessListener? = null

    private var mIsImageLoadSuccess: Boolean = false

    private var mMaxSize: Int = 500

    constructor(
        context: Context,
        imageStyle: ImageStyle,
        @DrawableRes defaultDrawableResId: Int,
        maxSize: Int,
        loadSuccessListener: OnImageLoadSuccessListener? = null
    ) : this(
        context,
        imageStyle,
        defaultDrawableResId,
        maxSize,
        loadSuccessListener,
        ALIGN_BASELINE
    )

    constructor(
        context: Context,
        imageStyle: ImageStyle,
        @DrawableRes defaultDrawableResId: Int,
        maxSize: Int,
        loadSuccessListener: OnImageLoadSuccessListener? = null,
        verticalAlignment: Int
    ) : super(
        context,
        defaultDrawableResId,
        verticalAlignment
    ) {
        mImageStyle = imageStyle
        mMaxSize = maxSize
        mOnImageLoadedListener = loadSuccessListener
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

    override fun getDrawable(): Drawable? {
        val context = mContext
        if (!mIsImageLoadSuccess && context != null && mOnImageLoadedListener != null) {
            Glide.with(context)
                .asBitmap()
                .load(mImageStyle.getRealPath(context))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val dest = resource.scale(mMaxSize, mMaxSize)
                        val bitmapDrawable = BitmapDrawable(context.resources, dest)
                        bitmapDrawable.setBounds(
                            0,
                            0,
                            bitmapDrawable.intrinsicWidth,
                            bitmapDrawable.intrinsicHeight
                        )
                        mDrawable = bitmapDrawable
                        mDrawableRef = WeakReference(null)
                        mIsImageLoadSuccess = true
                        mOnImageLoadedListener?.onLoadSuccess()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                })
        }
        return super.getDrawable()
    }

    override fun getStyle(): AbstractStyle {
        return mImageStyle
    }

    override fun getUBB(): String {
        return getStyle().toUBB(true)
    }

    /**
     * 该样式前置是否另起一行
     */
    override fun isStartSingleLine(): Boolean = true

    /**
     * 该样式后置是否另起一行
     */
    override fun isEndSingleLine(): Boolean = true

    /**
     * 图片加载成功回调
     */
    interface OnImageLoadSuccessListener {
        fun onLoadSuccess()
    }
}