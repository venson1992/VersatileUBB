package com.venson.versatile.ubb.holder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.widget.UBBContentView

/**
 * 图片
 */
class ImageViewHolder(itemView: ConstraintLayout) : AbcViewHolder(itemView) {

    val imageView: ShapeableImageView = ShapeableImageView(itemView.context)

    init {
        itemView.removeAllViews()
        itemView.addView(
            imageView,
            ConstraintLayout.LayoutParams(0, 0).also {
                it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    override fun bindData(
        adapter: UBBContentAdapter,
        ubbContentBean: UBBContentBean,
        parentWidth: Int
    ) {
        itemView.visibility = View.GONE
        val style = ubbContentBean.style ?: return
        val url = style.getAttr()[ImageStyle.ATTR_SRC] ?: return
        itemView.visibility = View.VISIBLE
        //自动宽度
        val imageWidth: Int = if (adapter.getImageWidth() <= 0) {
            if (adapter.getImageWidth() == UBBContentView.IMAGE_WIDTH_WRAP) {
                UBBContentView.IMAGE_WIDTH_WRAP
            } else {
                UBBContentView.IMAGE_WIDTH_MATCH
            }
        } else {
            adapter.getImageWidth()
        }
        imageView.layoutParams?.let { layoutParams ->
            layoutParams as ConstraintLayout.LayoutParams
            val align = style.getAlign(Paint.Align.CENTER)
            when {
                align.equals(Paint.Align.CENTER.name, true) -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
                align.equals(Paint.Align.LEFT.name, true) -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                }
                else -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
            }
            imageView.layoutParams = layoutParams
        }
        imageView.setTag(R.id.id_ubb_image, url)
        loadPlaceholder(adapter, adapter.getImageCorners(), url)
        val imageTarget = LoadImageTarget(
            url,
            imageWidth,
            parentWidth,
            adapter.getImageCorners(),
            adapter
        )
        Glide.with(imageView).asBitmap().load(url).into(imageTarget)
        imageView.setOnClickListener {
            mOnItemClickListener?.onClick(ubbContentBean.type, adapterPosition, it)
        }
    }

    /**
     * 加载缺省图
     */
    private fun loadPlaceholder(adapter: UBBContentAdapter, imageCorners: Float, url: String) {
        imageView.setImageBitmap(null)
        imageView.layoutParams?.let { layoutParams ->
            if (layoutParams is ConstraintLayout.LayoutParams) {
                val point = adapter.getSize(url)
                if (point != null) {
                    layoutParams.width = point.x
                    layoutParams.height = point.y
                    layoutParams.dimensionRatio = null
                } else {
                    layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                    layoutParams.height = 0
                    layoutParams.dimensionRatio = adapter.getImagePlaceholderRatio()
                }
                imageView.layoutParams = layoutParams
            }
        }
        imageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(CornerFamily.ROUNDED, imageCorners)
            .build()
        val placeholder = if (adapter.getImagePlaceholderRes() == 0) {
            R.drawable.default_drawable
        } else {
            adapter.getImagePlaceholderRes()
        }
        Glide.with(imageView)
            .load(placeholder)
            .into(imageView)
    }

    /**
     * 图片加载器，内部类
     */
    inner class LoadImageTarget(
        private val url: String,
        private val imageWidth: Int,
        private var itemWidth: Int,
        private val imageCorners: Float,
        private val adapter: UBBContentAdapter
    ) : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val tag = imageView.getTag(R.id.id_ubb_image) as? String
            if (tag?.equals(url) != true) {
                return
            }
            var resourceWidth = resource.width
            var resourceHeight = resource.height
            if (itemWidth == 0) {
                itemWidth = resourceWidth
            } else {
                //以1080p作为显示标准
                if (itemWidth > BASIC_WIDTH_PIXEL_MIN) {
                    resourceWidth = resourceWidth.times(itemWidth).div(BASIC_WIDTH_PIXEL)
                    resourceHeight = resourceHeight.times(itemWidth).div(BASIC_WIDTH_PIXEL)
                }
            }
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
            val displayHeight: Int = displayWidth.times(resourceHeight).div(resourceWidth)
            val point = Point(displayWidth, displayHeight)
            adapter.putSize(url, point)
            imageView.layoutParams?.let { layoutParams ->
                if (layoutParams is ConstraintLayout.LayoutParams) {
                    layoutParams.width = displayWidth
                    layoutParams.height = displayHeight
                    imageView.layoutParams = layoutParams
                }
            }
            val corners = imageCorners.times(displayWidth).div(itemWidth)
            imageView.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, corners)
                .build()
            Glide.with(imageView).asDrawable().load(url).into(imageView)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
//                    loadPlaceholder(holder, imageWidth)
        }

    }

    companion object {

        private const val BASIC_WIDTH_PIXEL = 1080
        private const val BASIC_WIDTH_PIXEL_MIN = 960//1080-45-45=990

        fun build(context: Context): ImageViewHolder {
            val itemView = ConstraintLayout(context)
            itemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            return ImageViewHolder(itemView)
        }
    }
}