package com.venson.versatile.ubb.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.holder.AudioViewHolder
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.holder.ImageViewHolder
import com.venson.versatile.ubb.holder.VideoViewHolder
import com.venson.versatile.ubb.style.ImageStyle

class UBBContentAdapter(private val mUBBContentList: List<UBBContentBean>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TEXT = 0x00
        const val TYPE_IMAGE = 0x12
        const val TYPE_AUDIO = 0x13
        const val TYPE_VIDEO = 0x14
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val customViewHolder = onCreateCustomViewHolder(parent, viewType)
        if (customViewHolder != null) {
            return customViewHolder
        }
        return when (viewType) {
            TYPE_IMAGE -> {
                ImageViewHolder.build(parent.context)
            }
            TYPE_VIDEO -> {
                VideoViewHolder.build(parent.context)
            }
            TYPE_AUDIO -> {
                AudioViewHolder.build(parent)
            }
            else -> {
                DefaultViewHolder.build(parent.context)
            }
        }
    }

    fun onCreateCustomViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ubbContentBean = getItem(position)
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
                    loadPlaceholder(holder, imageWidth)
                }

            }
            Glide.with(holder.imageView).asBitmap().load(url).into(imageTarget)
        }
        onBindViewHolder(holder, position, ubbContentBean)
    }

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

    fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        ubbContentBean: UBBContentBean
    ) {

    }

    fun getItem(position: Int): UBBContentBean {
        return mUBBContentList[position]
    }

    override fun getItemViewType(position: Int): Int {
        val ubbContentBean = getItem(position)
        return ubbContentBean.type
    }

    override fun getItemCount(): Int {
        return mUBBContentList.size
    }

}