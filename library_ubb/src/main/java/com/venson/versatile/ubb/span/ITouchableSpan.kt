package com.venson.versatile.ubb.span

import android.view.View

interface ITouchableSpan {
    fun setPressed(pressed: Boolean)
    fun onSpanClick(widget: View?)
}