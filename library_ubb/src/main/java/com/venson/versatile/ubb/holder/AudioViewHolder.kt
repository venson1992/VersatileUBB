package com.venson.versatile.ubb.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean

/**
 * 音频
 */
class AudioViewHolder(itemView: View) : AbcViewHolder(itemView) {

    companion object {

        fun build(parent: ViewGroup): AudioViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout_audio, parent, false)
            return AudioViewHolder(itemView)
        }
    }

    override fun bindData(
        adapter: UBBContentAdapter,
        ubbContentBean: UBBContentBean,
        parentWidth: Int
    ) {
        TODO("Not yet implemented")
    }
}