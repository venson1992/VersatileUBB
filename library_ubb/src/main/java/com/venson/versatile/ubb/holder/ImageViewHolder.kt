package com.venson.versatile.ubb.holder

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

/**
 * 图片
 */
class ImageViewHolder(itemView: ConstraintLayout) : RecyclerView.ViewHolder(itemView) {

    val imageView: ShapeableImageView = ShapeableImageView(itemView.context)

    @Px
    fun getCornerSize(): Float {
        return 0F
    }

    @DrawableRes
    fun getPlaceholderDrawableRes(): Int {
        return 0
    }

    fun getPlaceholderRatio(): String {
        return "2:1"
    }

    init {
        itemView.removeAllViews()
        itemView.addView(
            imageView,
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                0
            ).also {
                it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                it.dimensionRatio = getPlaceholderRatio()
                it.width = 0
                it.constrainedWidth = true
            }
        )
    }

    companion object {

        fun build(context: Context): ImageViewHolder {
            val itemView = ConstraintLayout(context)
            itemView.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            return ImageViewHolder(itemView)
        }
    }
}