package com.venson.versatile.ubb.holder

import android.content.Context
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.venson.versatile.ubb.widget.UBBContentView
import com.venson.versatile.ubb.widget.UBBTextView

/**
 * 文本
 */
class DefaultViewHolder(itemView: FrameLayout) : UBBContentView.ViewHolder(itemView) {

    companion object {

        fun build(context: Context): DefaultViewHolder {
            val itemView = FrameLayout(context)
            itemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            return DefaultViewHolder(itemView)
        }
    }

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
        textView.setTextIsSelectable(true)
    }

}