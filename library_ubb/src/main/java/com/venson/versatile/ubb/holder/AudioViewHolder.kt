package com.venson.versatile.ubb.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.venson.versatile.ubb.R

/**
 * 音频
 */
class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {

        fun build(parent: ViewGroup): AudioViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout_audio, parent, false)
            return AudioViewHolder(itemView)
        }
    }
}