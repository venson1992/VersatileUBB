package com.venson.versatile.ubb.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
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
        if (holder is DefaultViewHolder) {
            holder.textView.setSpannableText(ubbContentBean.text)
            return
        }
        if (holder is ImageViewHolder) {
            holder.itemView.visibility = View.GONE
            val style = ubbContentBean.style ?: return
            val url = style.getAttr()[ImageStyle.ATTR_SRC] ?: return
            val width = style.getAttr()[ImageStyle.ATTR_WIDTH] ?: ImageStyle.VALUE_AUTO
            val widthPercent: Float = if (width.endsWith("%")) {
                width.substring(0, width.length - 1).toFloatOrNull()?.div(100F) ?: 100F
            } else {
                100F
            }
            holder.itemView.visibility = View.VISIBLE
            val itemWidth = holder.itemView.measuredWidth
            //自动宽度
            val imageWidth = if (width == ImageStyle.VALUE_AUTO) {
                itemWidth
            } else {
                if (widthPercent >= 1F) {
                    itemWidth
                } else {
                    itemWidth * widthPercent.toInt()
                }
            }
            holder.imageView.setTag(R.id.id_ubb_image, url)
            holder.imageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, holder.getCornerSize())
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
                    val displayWidth: Int = if (width == ImageStyle.VALUE_AUTO) {
                        if (resourceWidth <= itemWidth) {
                            resourceWidth
                        } else {
                            itemWidth
                        }
                    } else if (widthPercent >= 1F) {
                        itemWidth
                    } else {
                        itemWidth * widthPercent.toInt()
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