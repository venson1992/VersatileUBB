package com.venson.versatile.ubb.holder

import android.content.Context
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.venson.versatile.ubb.widget.UBBTextView

/**
 * 文本
 */
class DefaultViewHolder(itemView: FrameLayout) : RecyclerView.ViewHolder(itemView) {

    val textView: UBBTextView = UBBTextView(itemView.context)

    init {
        itemView.removeAllViews()
        itemView.addView(
            textView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    companion object {

        fun build(context: Context): DefaultViewHolder {
            val itemView = FrameLayout(context)
            itemView.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            return DefaultViewHolder(itemView)
        }
    }
}