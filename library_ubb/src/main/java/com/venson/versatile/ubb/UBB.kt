package com.venson.versatile.ubb

import android.content.ContentResolver
import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt

/**
 * 一些配置项
 */
object UBB {

    const val SEPARATOR = "_" //应用和专题的id和标题的分隔符

    const val BREAK_LINE = '\n'//换行符

    const val SCHEME_CONTENT = ContentResolver.SCHEME_CONTENT

    const val SCHEME_FILE = ContentResolver.SCHEME_FILE

    const val SCHEME_DRAWABLE = ContentResolver.SCHEME_ANDROID_RESOURCE

    const val SCHEME_HTTP = "http"

    @ColorInt
    private var mClickableColor: Int = Color.parseColor("#03a9f4")

    private var LOG = true

    /**
     * 设置可点击的文本大小
     */
    fun setClickableColor(color: Int): UBB {
        mClickableColor = color
        return this
    }

    /**
     * 获取可点击文本颜色
     */
    fun getClickableColor(): Int {
        return mClickableColor
    }

    fun setLogEnable(enable: Boolean): UBB {
        LOG = enable
        return this
    }

    fun logEnable(): Boolean {
        return LOG
    }

    fun log(tag: String, message: String?) {
        message?.let {
            if (logEnable()) {
                Log.d(tag, it)
            }
        }
    }

}