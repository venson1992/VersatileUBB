package com.venson.versatile.ubb.ext

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * 获取普通颜色
 */
fun ColorStateList.getNormalColorColor(@ColorInt defaultColor: Int = Color.BLACK): Int {
    return getColorForState(intArrayOf(android.R.attr.state_enabled), defaultColor)
}

/**
 * 动态生成按压颜色
 */
fun createColorStateList(@ColorInt normalColor: Int, @ColorInt pressedColor: Int): ColorStateList {
    val colors = intArrayOf(
        pressedColor, normalColor, normalColor, normalColor, normalColor, normalColor
    )
    val states = arrayOf(
        intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_focused),
        intArrayOf(android.R.attr.state_window_focused),
        intArrayOf(),
    )
    return ColorStateList(states, colors)
}