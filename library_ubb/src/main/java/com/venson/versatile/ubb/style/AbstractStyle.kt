package com.venson.versatile.ubb.style

import android.graphics.Paint
import android.view.ViewGroup
import com.venson.versatile.ubb.widget.UBBContentView
import org.jsoup.nodes.Node

/**
 * 自定义span，数据类
 */
abstract class AbstractStyle {

    //属性map
    private val attrMap = hashMapOf<String, String>()

    //值
    private var text: String = ""

    abstract fun getHelper(): Helper

    /**
     * 存入属性键值对
     */
    fun putAttr(key: String, value: String) {
        attrMap[key] = value
    }

    /**
     * 获取属性map
     */
    fun getAttr() = attrMap

    /**
     * 获取键对应的属性值
     */
    fun getAttr(key: String, value: String = ""): String {
        return getAttr()[key] ?: value
    }

    /**
     * 设置展示的文本
     */
    fun setText(text: String) {
        this.text = text
    }

    /**
     * 获取展示的文本
     */
    fun getText() = text

    /**
     * content转ubb
     */
    fun toUBB(): String {
        val ubb = StringBuilder()
        ubb.append("[${getHelper().getTagName()}")
        if (attrMap.isNotEmpty()) {
            attrMap.forEach { entry ->
                ubb.append(" ${entry.key}=\"${entry.value}\"")
            }
        }
        ubb.append("]").append(text).append("[/${getHelper().getTagName()}]")
        return ubb.toString()
    }

    /**
     * 根据数据获取span
     */
    abstract fun getSpan(): Any?

    /**
     * 应用样式后显示的文本内容
     */
    open fun getSpanText(): String {
        return getText()
    }

    abstract fun getViewHolder(parent: ViewGroup): UBBContentView.ViewHolder?

    abstract class Helper {

        /**
         * 自定义的ubb标签
         */
        abstract fun getTagName(): String

        /**
         * 是否属于该标签
         */
        fun equalsTag(tagName: String): Boolean = getTagName().equals(tagName, true)

        /**
         * ViewHolder
         */
        abstract fun getViewType(): Int

        /**
         * 转换该ubb标签为html
         */
        abstract fun convertUBB(source: String): String

        /**
         * 解析node
         */
        abstract fun fromUBB(node: Node?, align: Paint.Align = Paint.Align.LEFT): AbstractStyle
    }

}