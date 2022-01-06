package com.venson.versatile.ubb.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class HeadOrFootAdapter(val view: View) :
    RecyclerView.Adapter<HeadOrFootAdapter.HeadOrFootHolder>() {

    @Px
    private var mHeadSpacing: Int = 0

    @Px
    private var mFootSpacing: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateSpacing(headSpacing: Int, footSpacing: Int) {
        mHeadSpacing = headSpacing
        mFootSpacing = footSpacing
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadOrFootHolder {
        view.layoutParams?.let { layoutParams ->
            layoutParams as ViewGroup.MarginLayoutParams
            if (mHeadSpacing > 0) {
                layoutParams.bottomMargin = mHeadSpacing
            }
            if (mFootSpacing > 0) {
                layoutParams.topMargin = mFootSpacing
            }
            view.layoutParams = layoutParams
        }
        return HeadOrFootHolder(view)
    }

    override fun onBindViewHolder(holder: HeadOrFootHolder, position: Int) {

    }

    override fun getItemCount(): Int = 1

    inner class HeadOrFootHolder(view: View) : RecyclerView.ViewHolder(view)

}