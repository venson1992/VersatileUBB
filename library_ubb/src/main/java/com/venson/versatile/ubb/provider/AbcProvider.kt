package com.venson.versatile.ubb.provider

import android.content.Context
import android.graphics.Paint
import com.venson.versatile.ubb.holder.AbcViewHolder
import com.venson.versatile.ubb.style.AbstractStyle
import org.jsoup.nodes.Node
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 提供解析方式、样式的抽象方法类
 */
abstract class AbcProvider<VH : AbcViewHolder> {

    var type: Type = Any::class.java

    init {
        val superClass = this.javaClass.genericSuperclass
        type = (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * UBB标签名
     */
    abstract fun getUBBTagName(): String

    /**
     * 是否属于该标签
     */
    fun equalsTag(tagName: String): Boolean = getUBBTagName().equals(tagName, true)

    /**
     * 创建视图holder
     */
    abstract fun createViewHolder(context: Context): VH

    /**
     * ubb转html
     */
    abstract fun convert2UBB(source: String): String

    /**
     * html转style
     */
    abstract fun parse2Style(node: Node?, align: Paint.Align = Paint.Align.LEFT): AbstractStyle

}