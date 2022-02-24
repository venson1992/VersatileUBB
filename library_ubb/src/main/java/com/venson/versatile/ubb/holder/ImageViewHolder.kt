package com.venson.versatile.ubb.holder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.Log
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
        loadPlaceholder(
            adapter,
            adapter.getImageCorners(),
            adapter.getImageCornersEnableFlags(),
            url,
            bindingAdapterPosition == 0,
            bindingAdapterPosition == adapter.itemCount - 1
        )
        val imageTarget = LoadImageTarget(
            url,
            imageWidth,
            parentWidth,
            adapter.getImageCorners(),
            adapter.getImageCornersEnableFlags(),
            adapter,
            bindingAdapterPosition == 0,
            bindingAdapterPosition == adapter.itemCount - 1
        )
        Glide.with(imageView).asBitmap().load(url).into(imageTarget)
        imageView.setOnClickListener {
            mOnItemClickListener?.onClick(ubbContentBean.type, adapterPosition, it)
        }
    }

    /**
     * 加载缺省图
     */
    private fun loadPlaceholder(
        adapter: UBBContentAdapter,
        imageCorners: Array<Float>,
        imageCornerEnableFlags: Int,
        url: String,
        isFirst: Boolean,
        isLast: Boolean
    ) {
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
        imageView.shapeAppearanceModel = getCornersBuild(
            1,
            1,
            imageCorners,
            imageCornerEnableFlags,
            isFirst,
            isLast
        )
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
        private val imageCorners: Array<Float>,
        private val imageCornerEnableFlags: Int,
        private val adapter: UBBContentAdapter,
        private val isFirst: Boolean,
        private val isLast: Boolean
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
            imageView.shapeAppearanceModel = getCornersBuild(
                displayWidth,
                itemWidth,
                imageCorners,
                imageCornerEnableFlags,
                isFirst,
                isLast
            )
            Glide.with(imageView).asDrawable().load(url).into(imageView)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
//                    loadPlaceholder(holder, imageWidth)
        }

    }

    private fun getCornersBuild(
        displayWidth: Int,
        itemWidth: Int,
        imageCorners: Array<Float>,
        imageCornerEnableFlags: Int,
        isFirst: Boolean,
        isLast: Boolean
    ): ShapeAppearanceModel {
        return ShapeAppearanceModel.builder().apply {
            val topLeftCorner = imageCorners[0].times(displayWidth).div(itemWidth)
            val topRightCorner = imageCorners[1].times(displayWidth).div(itemWidth)
            val bottomRightCorner = imageCorners[2].times(displayWidth).div(itemWidth)
            val bottomLeftCorner = imageCorners[3].times(displayWidth).div(itemWidth)
            if (imageCornerEnableFlags.or(UBBContentView.IMAGE_CORNERS_ENABLE_ANY_FLAG)
                == imageCornerEnableFlags
            ) {
                setTopLeftCorner(CornerFamily.ROUNDED, topLeftCorner)
                setTopRightCorner(CornerFamily.ROUNDED, topRightCorner)
                setBottomRightCorner(CornerFamily.ROUNDED, bottomRightCorner)
                setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftCorner)
                return@apply
            }
            if (isFirst) {
                if (imageCornerEnableFlags.or(UBBContentView.IMAGE_CORNERS_ENABLE_FIRST_FLAG)
                    == imageCornerEnableFlags
                    || imageCornerEnableFlags.or(UBBContentView.IMAGE_CORNERS_ENABLE_EDGE_FLAG)
                    == imageCornerEnableFlags
                ) {
                    setTopLeftCorner(CornerFamily.ROUNDED, topLeftCorner)
                    setTopRightCorner(CornerFamily.ROUNDED, topRightCorner)
                }
            }
            if (isLast) {
                if (imageCornerEnableFlags.or(UBBContentView.IMAGE_CORNERS_ENABLE_LAST_FLAG)
                    == imageCornerEnableFlags
                    || imageCornerEnableFlags.or(UBBContentView.IMAGE_CORNERS_ENABLE_EDGE_FLAG)
                    == imageCornerEnableFlags
                ) {
                    setBottomRightCorner(CornerFamily.ROUNDED, bottomRightCorner)
                    setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftCorner)
                }
            }
        }.build()
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