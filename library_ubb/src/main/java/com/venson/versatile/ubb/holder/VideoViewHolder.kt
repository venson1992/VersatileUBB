package com.venson.versatile.ubb.holder

import android.content.Context
import android.view.Gravity
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.venson.versatile.ubb.widget.UBBContentView

/**
 * 视频
 */
class VideoViewHolder(itemView: FrameLayout) : UBBContentView.ViewHolder(itemView) {

    val webView: WebView = WebView(itemView.context)

    init {
        itemView.removeAllViews()
        itemView.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER
            }
        )
    }

    companion object {

        fun build(context: Context): VideoViewHolder {
            val itemView = FrameLayout(context)
            itemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            return VideoViewHolder(itemView)
        }
    }
}