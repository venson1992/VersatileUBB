package com.venson.versatile.ubb.style

import android.graphics.Paint
import org.jsoup.nodes.Node
import java.util.*

/**
 * 自定义span，数据类
 */
abstract class AbstractStyle {

    protected val ATTR_ALIGN = "align"

    // 属性map
    private val attrMap = hashMapOf<String, String>()

    // 值
    private var text: String = ""

    /**
     * 标签名字
     */
    abstract fun getTagName(): String

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
     * 解析ubb
     */
    open fun fromUBB(node: Node?, align: Paint.Align) {
        putAttr(ATTR_ALIGN, align.name.toLowerCase(Locale.ROOT))
        node?.attributes()?.forEach {
            putAttr(it.key, it.value)
        }
    }

    /**
     * content转ubb
     */
    fun toUBB(isWithAlign: Boolean): String {
        val ubb = StringBuilder()
        val align = getAttr(ATTR_ALIGN)
        val isAlignLeft = align.equals("left", true)
        if (isWithAlign && !isAlignLeft) {
            ubb.append("[align=${getAttr(ATTR_ALIGN)}]")
        }
        ubb.append("[${getTagName()}")
        if (attrMap.isNotEmpty()) {
            attrMap.forEach { entry ->
                ubb.append(" ${entry.key}=\"${entry.value}\"")
            }
        }
        ubb.append("]")
            .append(text)
            .append("[/${getTagName()}]")
        if (isWithAlign && !isAlignLeft) {
            ubb.append("[/align]")
        }
        return ubb.toString()
    }

    fun getAlign(defaultAlign: Paint.Align): String {
        return getAttr(ATTR_ALIGN, defaultAlign.name.toLowerCase(Locale.ROOT))
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

}