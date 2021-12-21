package com.venson.versatile.ubb.holder

import android.content.Context
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.widget.UBBTextView

/**
 * 文本
 */
class DefaultViewHolder(itemView: FrameLayout) : AbcViewHolder(itemView) {

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

    override fun bindData(
        adapter: UBBContentAdapter,
        ubbContentBean: UBBContentBean,
        parentWidth: Int
    ) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, adapter.getTextSize())
        textView.setTextColor(adapter.getTextColor())
        textView.setLineSpacing(adapter.getLineSpacingExtra(), adapter.getLineSpacingMultiplier())
        textView.setSpannableText(ubbContentBean.text)
    }

}