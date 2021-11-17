package com.venson.versatile.ubb.span

import android.graphics.Paint
import com.aiwu.market.ubb.span.BaseLineEnableSpan
import com.venson.versatile.ubb.UBB

class AtSpan(spanContent: SpanContent, align: Paint.Align = Paint.Align.LEFT) :
    BaseLineEnableSpan(spanContent, align, UBB.getClickableColor()) {

    companion object {
        const val TAG_NAME = "user"
    }

    override fun getTag(): String = TAG_NAME
}