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
    internal fun updateMargin(position: Int, verticalSpacing: Int) {
        val topMargin = if (position == 0) {
            0
        } else {
            verticalSpacing
        }
        itemView.layoutParams?.let { layoutParams ->
            layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = topMargin
            itemView.layoutParams = layoutParams
        }
    }

    fun setOnClickListener(listener: UBBContentAdapter.OnItemClickListener) {
        mOnItemClickListener = listener
    }
}