package com.venson.versatile.ubb.span

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * 重写[ImageSpan]
 */
open class DynamicImageSpan : DynamicDrawableSpan {

    protected var mDrawable: Drawable? = null
    protected var mDrawableRef: WeakReference<Drawable>? = null
    private var mContentUri: Uri? = null

    @DrawableRes
    private var mResourceId = 0
    protected var mContext: Context? = null
    private var mSource: String? = null

    /**
     * Constructs an [DynamicImageSpan] from a [Context] and a [Bitmap] with the default
     * alignment [DynamicDrawableSpan.ALIGN_BOTTOM]
     *
     * @param context context used to create a drawable from {@param bitmap} based on the display
     * metrics of the resources
     * @param bitmap  bitmap to be rendered
     */
    constructor(context: Context, bitmap: Bitmap) : this(context, bitmap, ALIGN_BOTTOM)

    /**
     * Constructs an [DynamicImageSpan] from a [Context], a [Bitmap] and a vertical
     * alignment.
     *
     * @param context           context used to create a drawable from {@param bitmap} based on
     * the display metrics of the resources
     * @param bitmap            bitmap to be rendered
     * @param verticalAlignment one of [DynamicDrawableSpan.ALIGN_BOTTOM] or
     * [DynamicDrawableSpan.ALIGN_BASELINE]
     */
    constructor(
        context: Context,
        bitmap: Bitmap,
        verticalAlignment: Int
    ) : super(verticalAlignment) {
        mContext = context
        mDrawable = BitmapDrawable(context.resources, bitmap).also {
            val width = it.intrinsicWidth
            val height = it.intrinsicHeight
            mDrawable?.setBounds(
                0,
                0,
                if (width > 0) {
                    width
                } else {
                    0
                },
                if (height > 0) {
                    height
                } else {
                    0
                }
            )
        }
    }

    /**
     * Constructs an [DynamicImageSpan] from a drawable with the default
     * alignment [DynamicDrawableSpan.ALIGN_BOTTOM].
     *
     * @param drawable drawable to be rendered
     */
    constructor(drawable: Drawable) : this(drawable, ALIGN_BOTTOM)

    /**
     * Constructs an [DynamicImageSpan] from a drawable and a vertical alignment.
     *
     * @param drawable          drawable to be rendered
     * @param verticalAlignment one of [DynamicDrawableSpan.ALIGN_BOTTOM] or
     * [DynamicDrawableSpan.ALIGN_BASELINE]
     */
    constructor(drawable: Drawable, verticalAlignment: Int) : super(verticalAlignment) {
        mDrawable = drawable
    }

    /**
     * Constructs an [DynamicImageSpan] from a drawable and a source with the default
     * alignment [DynamicDrawableSpan.ALIGN_BOTTOM]
     *
     * @param drawable drawable to be rendered
     * @param source   drawable's Uri source
     */
    constructor(drawable: Drawable, source: String) : this(drawable, source, ALIGN_BOTTOM)

    /**
     * Constructs an [DynamicImageSpan] from a drawable, a source and a vertical alignment.
     *
     * @param drawable          drawable to be rendered
     * @param source            drawable's uri source
     * @param verticalAlignment one of [DynamicDrawableSpan.ALIGN_BOTTOM] or
     * [DynamicDrawableSpan.ALIGN_BASELINE]
     */
    constructor(drawable: Drawable, source: String, verticalAlignment: Int) :
            super(verticalAlignment) {
        mDrawable = drawable
        mSource = source
    }

    /**
     * Constructs an [DynamicImageSpan] from a [Context] and a [Uri] with the default
     * alignment [DynamicDrawableSpan.ALIGN_BOTTOM]. The Uri source can be retrieved via
     * [.getSource]
     *
     * @param context context used to create a drawable from {@param bitmap} based on the display
     * metrics of the resources
     * @param uri     [Uri] used to construct the drawable that will be rendered
     */
    constructor(context: Context, uri: Uri) : this(context, uri, ALIGN_BOTTOM)

    /**
     * Constructs an [DynamicImageSpan] from a [Context], a [Uri] and a vertical
     * alignment. The Uri source can be retrieved via [.getSource]
     *
     * @param context           context used to create a drawable from {@param bitmap} based on
     * the display
     * metrics of the resources
     * @param uri               [Uri] used to construct the drawable that will be rendered.
     * @param verticalAlignment one of [DynamicDrawableSpan.ALIGN_BOTTOM] or
     * [DynamicDrawableSpan.ALIGN_BASELINE]
     */
    constructor(context: Context, uri: Uri, verticalAlignment: Int) : super(verticalAlignment) {
        mContext = context
        mContentUri = uri
        mSource = uri.toString()
    }

    /**
     * Constructs an [DynamicImageSpan] from a [Context] and a resource id with the default
     * alignment [DynamicDrawableSpan.ALIGN_BOTTOM]
     *
     * @param context    context used to retrieve the drawable from resources
     * @param resourceId drawable resource id based on which the drawable is retrieved
     */
    constructor(context: Context, @DrawableRes resourceId: Int) :
            this(context, resourceId, ALIGN_BOTTOM)

    /**
     * Constructs an [DynamicImageSpan] from a [Context], a resource id and a vertical
     * alignment.
     *
     * @param context           context used to retrieve the drawable from resources
     * @param resourceId        drawable resource id based on which the drawable is retrieved.
     * @param verticalAlignment one of [DynamicDrawableSpan.ALIGN_BOTTOM] or
     * [DynamicDrawableSpan.ALIGN_BASELINE]
     */
    constructor(context: Context, @DrawableRes resourceId: Int, verticalAlignment: Int) :
            super(verticalAlignment) {
        mContext = context
        mResourceId = resourceId
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        val d = getCachedDrawable()
        val rect = d!!.bounds
        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }
        return rect.right
    }

    override fun getDrawable(): Drawable? {
        var drawable: Drawable? = null
        val context = mContext ?: return drawable
        when {
            mDrawable != null -> {
                drawable = mDrawable
            }
            mContentUri != null -> {
                val bitmap: Bitmap?
                try {
                    val `is` = context.contentResolver.openInputStream(
                        mContentUri!!
                    )
                    bitmap = BitmapFactory.decodeStream(`is`)
                    drawable = BitmapDrawable(context.resources, bitmap)
                    drawable.setBounds(
                        0,
                        0,
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight()
                    )
                    `is`!!.close()
                } catch (e: Exception) {
                    Log.e("DynamicImageSpan", "Failed to loaded content $mContentUri", e)
                }
            }
            else -> {
                try {
                    drawable = ContextCompat.getDrawable(context, mResourceId)?.also {
                        it.setBounds(
                            0, 0, it.intrinsicWidth, it.intrinsicHeight
                        )
                    }
                } catch (e: Exception) {
                    Log.e("DynamicImageSpan", "Unable to find resource: $mResourceId")
                }
            }
        }
        return drawable
    }

    protected fun getCachedDrawable(): Drawable? {
        val wr = mDrawableRef
        var d: Drawable? = null
        if (wr != null) {
            d = wr.get()
        }
        if (d == null) {
            d = drawable
            mDrawableRef = WeakReference(d)
        }
        return d
    }

    /**
     * Returns the source string that was saved during construction.
     *
     * @return the source string that was saved during construction
     * @see .ImageSpan
     * @see .ImageSpan
     */
    fun getSource(): String? {
        return mSource
    }
}