package com.venson.versatile.ubb.holder

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean

/**
 * 样式视图
 */
abstract class AbcViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected var mOnItemClickListener: UBBContentAdapter.OnItemClickListener? = null

    internal abstract fun bindData(
        adapter: UBBContentAdapter,
        ubbContentBean: UBBContentBean,
        parentWidth: Int
    )

    /**
     * 设置间距
     */
    internal fun updateMargin(
        position: Int,
        dataCount: Int,
        verticalEdgeSpacing: Int,
        verticalSpacing: Int,
        horizontalSpacing: Int
    ) {
        val topMargin = if (position == 0) {
            verticalEdgeSpacing
        } else {
            verticalSpacing
        }
        val bottomMargin = if (position == dataCount - 1) {
            verticalEdgeSpacing
        } else {
            0
        }
        itemView.layoutParams?.let { layoutParams ->
            layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = topMargin
            layoutParams.bottomMargin = bottomMargin
            layoutParams.marginStart = horizontalSpacing
            layoutParams.marginEnd = horizontalSpacing
            layoutParams.leftMargin = horizontalSpacing
            layoutParams.rightMargin = horizontalSpacing
            itemView.layoutParams = layoutParams
        }
    }

    fun setOnClickListener(listener: UBBContentAdapter.OnItemClickListener) {
        mOnItemClickListener = listener
    }
}