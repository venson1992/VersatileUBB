package com.venson.versatile.ubb.holder

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.imageview.ShapeableImageView
import com.venson.versatile.ubb.widget.UBBContentView

/**
 * 图片
 */
class ImageViewHolder(itemView: ConstraintLayout) : UBBContentView.ViewHolder(itemView) {

    val imageView: ShapeableImageView = ShapeableImageView(itemView.context)

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
                it.width = 0
                it.constrainedWidth = true
            }
        )
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
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