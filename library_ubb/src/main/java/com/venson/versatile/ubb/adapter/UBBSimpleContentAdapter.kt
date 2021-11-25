package com.venson.versatile.ubb.adapter

import android.view.ViewGroup
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.widget.UBBContentView

class UBBSimpleContentAdapter : UBBContentAdapter() {

    override fun isCustomSpan(span: Any): Boolean {
        return false
    }

    override fun onCreateCustomViewHolder(
        parent: ViewGroup,
        customStyle: AbstractStyle
    ): UBBContentView.ViewHolder? {
        return null
    }

    override fun onBindCustomViewHolder(
        holder: UBBContentView.ViewHolder,
        position: Int,
        ubbContentBean: UBBContentBean
    ) {

    }

    override fun getTypeByStyle(customStyle: AbstractStyle): Int {
        return customStyle.getHelper().getViewHolderType()
    }
}