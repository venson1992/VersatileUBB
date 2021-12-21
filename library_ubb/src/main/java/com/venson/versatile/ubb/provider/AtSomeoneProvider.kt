package com.venson.versatile.ubb.provider

import android.content.Context
import android.graphics.Paint
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.AtSomeoneStyle
import com.venson.versatile.ubb.utils.convertHTML
import org.jsoup.nodes.Node

/**
 * at某人解析对象提供器
 * ```[user=id]name[/user]```
 */
class AtSomeoneProvider : AbcProvider<DefaultViewHolder>() {

    override fun getUBBTagName(): String = AtSomeoneStyle.TAG_NAME

    override fun createViewHolder(context: Context): DefaultViewHolder {
        return DefaultViewHolder.build(context)
    }

    override fun convert2UBB(source: String): String {
        return convertHTML(
            source,
            "${getUBBTagName()}=,(.+?)",
            getUBBTagName(),
            "<${getUBBTagName()} ${AtSomeoneStyle.ATTR_USER}=\"",
            "\"></${getUBBTagName()}>"
        )
    }

    override fun parse2Style(
        node: Node?,
        align: Paint.Align
    ): AbstractStyle = AtSomeoneStyle(align).also {
        it.fromUBB(node, align)
    }

}