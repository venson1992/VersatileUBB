package com.venson.versatile.ubb.provider

import android.content.Context
import android.graphics.Paint
import com.venson.versatile.ubb.holder.ImageViewHolder
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.utils.convertHTML
import org.jsoup.nodes.Node

/**
 * 图片展示解析提供器
 * ```[img]url[/img]```
 */
class ImageProvider : AbcProvider<ImageViewHolder>() {

    override fun getUBBTagName(): String = ImageStyle.TAG_NAME

    override fun createViewHolder(context: Context): ImageViewHolder {
        return ImageViewHolder.build(context)
    }

    override fun convert2UBB(source: String): String {
        val tagStart = "<${getUBBTagName()} " +
                "${ImageStyle.ATTR_WIDTH}=100%; " +
                "${ImageStyle.ATTR_HEIGHT}=${ImageStyle.VALUE_AUTO}; " +
                "alt=\"\" " +
                "${ImageStyle.ATTR_SRC}=\""
        val tagEnd = "\"></${getUBBTagName()}>"
        var dest = source
        dest = convertHTML(
            dest,
            getUBBTagName(),
            getUBBTagName(),
            tagStart,
            tagEnd
        )
        dest = convertHTML(
            dest,
            "${getUBBTagName()}=,(.+?)",
            getUBBTagName(),
            tagStart,
            tagEnd
        )
        return dest
    }

    override fun parse2Style(node: Node?, align: Paint.Align): AbstractStyle {
        return ImageStyle().also {
            it.fromUBB(node, align)
        }
    }
}