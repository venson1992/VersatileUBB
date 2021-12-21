package com.venson.versatile.ubb

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt
import com.venson.versatile.ubb.holder.AbcViewHolder
import com.venson.versatile.ubb.provider.AbcProvider
import com.venson.versatile.ubb.provider.AtSomeoneProvider
import com.venson.versatile.ubb.provider.ImageProvider

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

    private var applicationContext: Context? = null

    private var imageEngine: ImageEngine? = null

    private val mProviderMapGroupByType: MutableMap<Int, AbcProvider<out AbcViewHolder>> by lazy {
        mutableMapOf()
    }
    private val mProviderMapGroupByTagName: MutableMap<String, AbcProvider<out AbcViewHolder>> by lazy {
        mutableMapOf<String, AbcProvider<out AbcViewHolder>>().also {
            mProviderMapGroupByType.clear()
            addProvider(it, ImageProvider())
            addProvider(it, AtSomeoneProvider())
        }
    }

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

    fun logV(tag: String, message: String?) {
        message?.let {
            if (logEnable()) {
                Log.v(tag, it)
            }
        }
    }

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun getApplicationContext(): Context? = applicationContext

    fun setImageEngine(imageEngine: ImageEngine) {
        this.imageEngine = imageEngine
    }

    fun getImageEngine(): ImageEngine? = imageEngine

    fun addProvider(
        map: MutableMap<String, AbcProvider<out AbcViewHolder>>,
        provider: AbcProvider<out AbcViewHolder>
    ) {
        map[provider.getUBBTagName()] = provider
        mProviderMapGroupByType[getViewType(provider)] = provider
    }

    fun getProviderMap(): MutableMap<String, AbcProvider<out AbcViewHolder>> =
        mProviderMapGroupByTagName

    fun getProvider(tagName: String): AbcProvider<out AbcViewHolder>? =
        mProviderMapGroupByTagName[tagName]

    fun getProvider(type: Int): AbcProvider<out AbcViewHolder>? = mProviderMapGroupByType[type]

    fun getViewType(clazz: Class<out AbcViewHolder>): Int {
        return clazz.toString().hashCode()
    }

    fun getViewType(provider: AbcProvider<out AbcViewHolder>): Int {
        return provider.type.toString().hashCode()
    }
}