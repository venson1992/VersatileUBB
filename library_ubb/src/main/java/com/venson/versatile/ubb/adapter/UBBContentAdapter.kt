package com.venson.versatile.ubb.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.holder.ImageViewHolder
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.widget.UBBContentView

abstract class UBBContentAdapter {

    @ColorInt
    private var mTextColor: Int = Color.BLACK

    @Px
    private var mTextSize: Float = 0F

    @Px
    private var mLineSpacingExtra: Float = 0F

    @Px
    private var mLineSpacingMultiplier: Float = 1.0F

    @Px
    private var mVerticalSpacing: Int = 0

    @Px
    private var mImageCorners: Float = 0F

    @Px
    private var mImageWidth: Int = UBBContentView.IMAGE_WIDTH_MATCH

    @DrawableRes
    private var mImagePlaceholderRes: Int = 0

    private var mImagePlaceholderRatio: String = "2:1"

    internal fun initParams(
        textColor: Int,
        textSize: Float,
        lineSpacingExtra: Float,
        lineSpacingMultiplier: Float,
        verticalSpacing: Int,
        imageCorners: Float,
        imageWidth: Int
    ) {
        mTextColor = textColor
        mTextSize = textSize
        mLineSpacingExtra = lineSpacingExtra
        mLineSpacingMultiplier = lineSpacingMultiplier
        mVerticalSpacing = verticalSpacing
        mImageCorners = imageCorners
        mImageWidth = imageWidth
    }

    @ColorInt
    fun getTextColor(): Int = mTextColor

    @Px
    fun getTextSize(): Float = mTextSize

    @Px
    fun getLineSpacingExtra(): Float = mLineSpacingExtra

    @Px
    fun getLineSpacingMultiplier(): Float = mLineSpacingMultiplier

    @Px
    fun getVerticalSpacing(): Int = mVerticalSpacing

    @Px
    fun getImageCorners(): Float = mImageCorners

    @Px
    fun getImageWidth(): Int = mImageWidth

    fun onCreateViewHolder(
        parent: ViewGroup,
        customStyle: AbstractStyle
    ): UBBContentView.ViewHolder? {
        val customViewHolder = onCreateCustomViewHolder(parent, customStyle)
        if (customViewHolder != null) {
            return customViewHolder
        }
        return customStyle.getViewHolder(parent)
    }

    abstract fun isCustomSpan(span: Any): Boolean

    abstract fun onCreateCustomViewHolder(
        parent: ViewGroup,
        customStyle: AbstractStyle
    ): UBBContentView.ViewHolder?

    fun onBindViewHolder(
        holder: UBBContentView.ViewHolder,
        position: Int,
        ubbContentBean: UBBContentBean
    ) {
        /*
        设置间隔
         */
        val topMargin = if (position == 0) {
            0
        } else {
            getVerticalSpacing()
        }
        holder.itemView.layoutParams?.let { layoutParams ->
            layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = topMargin
            holder.itemView.layoutParams = layoutParams
        }
        /*
        其他
         */
        if (holder is DefaultViewHolder) {
            holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
            holder.textView.setTextColor(getTextColor())
            holder.textView.setLineSpacing(getLineSpacingExtra(), mLineSpacingMultiplier)
            holder.textView.setSpannableText(ubbContentBean.text)
            return
        }
        if (holder is ImageViewHolder) {
            holder.itemView.visibility = View.GONE
            val style = ubbContentBean.style ?: return
            val url = style.getAttr()[ImageStyle.ATTR_SRC] ?: return
            holder.itemView.visibility = View.VISIBLE
            val itemWidth = holder.itemView.measuredWidth
            //自动宽度
            val imageWidth: Int = if (getImageWidth() <= 0) {
                if (getImageWidth() == UBBContentView.IMAGE_WIDTH_WRAP) {
                    UBBContentView.IMAGE_WIDTH_WRAP
                } else {
                    UBBContentView.IMAGE_WIDTH_MATCH
                }
            } else {
                getImageWidth()
            }
            holder.imageView.layoutParams?.let { layoutParams ->
                layoutParams as ConstraintLayout.LayoutParams
                when (style.getAttr(ImageStyle.ATTR_ALIGN, Paint.Align.CENTER.name)) {
                    Paint.Align.CENTER.name -> {
                        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                    Paint.Align.LEFT.name -> {
                        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                    }
                    else -> {
                        layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                }
                holder.imageView.layoutParams = layoutParams
            }
            holder.imageView.setTag(R.id.id_ubb_image, url)
            holder.imageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, getImageCorners())
                .build()
            loadPlaceholder(holder, imageWidth)
            val imageTarget = object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val tag = holder.imageView.getTag(R.id.id_ubb_image) as? String
                    if (tag?.equals(url) != true) {
                        return
                    }
                    val resourceWidth = resource.width
                    val resourceHeight = resource.height
                    val displayWidth: Int = if (imageWidth == UBBContentView.IMAGE_WIDTH_MATCH) {
                        itemWidth
                    } else if (imageWidth == UBBContentView.IMAGE_WIDTH_WRAP) {
                        if (resourceWidth >= itemWidth) {
                            itemWidth
                        } else {
                            resourceWidth
                        }
                    } else {
                        imageWidth
                    }
                    holder.imageView.layoutParams?.let { layoutParams ->
                        if (layoutParams is ConstraintLayout.LayoutParams) {
                            layoutParams.width = displayWidth
                            layoutParams.dimensionRatio = "$resourceWidth:$resourceHeight"
                            holder.imageView.layoutParams = layoutParams
                        }
                    }
                    Glide.with(holder.imageView).load(url).into(holder.imageView)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
//                    loadPlaceholder(holder, imageWidth)
                }

            }
            Glide.with(holder.imageView).asBitmap().load(url).into(imageTarget)
        }
        onBindCustomViewHolder(holder, position, ubbContentBean)
    }

    abstract fun onBindCustomViewHolder(
        holder: UBBContentView.ViewHolder,
        position: Int,
        ubbContentBean: UBBContentBean
    )

    abstract fun getTypeByStyle(customStyle: AbstractStyle): Int

    private fun loadPlaceholder(holder: ImageViewHolder, imageWidth: Int) {
        holder.imageView.setImageBitmap(null)
        holder.imageView.layoutParams?.let { layoutParams ->
            if (layoutParams is ConstraintLayout.LayoutParams) {
                layoutParams.width = imageWidth
                layoutParams.dimensionRatio = holder.getPlaceholderRatio()
                holder.imageView.layoutParams = layoutParams
            }
        }
        Glide.with(holder.imageView)
            .load(holder.getPlaceholderDrawableRes())
            .into(holder.imageView)
    }

}