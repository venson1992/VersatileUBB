package com.venson.versatile.ubb.holder

import android.content.Context
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.imageview.ShapeableImageView
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.widget.UBBContentView

/**
 * 图片
 */
class ImageViewHolder(itemView: ConstraintLayout) : UBBContentView.ViewHolder(itemView) {

    val imageView: ShapeableImageView = ShapeableImageView(itemView.context)

    @DrawableRes
    fun getPlaceholderDrawableRes(): Int {
        return R.drawable.default_drawable
    }

    fun getPlaceholderRatio(): String {
        return "4:3"
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
            itemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            return ImageViewHolder(itemView)
        }
    }
}