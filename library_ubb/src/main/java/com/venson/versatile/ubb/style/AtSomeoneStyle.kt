package com.venson.versatile.ubb.style

import android.graphics.Paint
import android.view.ViewGroup
import com.venson.versatile.ubb.bean.UBBViewType
import com.venson.versatile.ubb.span.AtSomeoneSpan
import com.venson.versatile.ubb.utils.convertHTML
import com.venson.versatile.ubb.widget.UBBContentView
import org.jsoup.nodes.Node

/**
 * AT用户样式
 */
class AtSomeoneStyle(private val align: Paint.Align = Paint.Align.LEFT) : AbstractStyle() {

    companion object {
        private const val TAG_NAME = "user"
        private const val ATTR_USER = "user"
    }

    override fun getHelper(): AbstractStyle.Helper {
        return Helper
    }

    override fun getSpan(): Any {
        return AtSomeoneSpan(this, align)
    }

    override fun getSpanText(): String {
        return "@" + super.getSpanText()
    }

    override fun getViewHolder(parent: ViewGroup): UBBContentView.ViewHolder? {
        return null
    }

    object Helper : AbstractStyle.Helper() {

        override fun getTagName(): String = TAG_NAME

        override fun getViewType(): Int {
            return UBBViewType.VIEW_TEXT.type
        }

        override fun convertUBB(source: String): String {
            return convertHTML(
                source,
                "${getTagName()}=,(.+?)",
                getTagName(),
                "<${getTagName()} $ATTR_USER=\"",
                "\"></${getTagName()}>"
            )
        }

        override fun fromUBB(node: Node?, align: Paint.Align): AbstractStyle {
            return AtSomeoneStyle(align).also {
                if (node == null) {
                    return@also
                }
                it.putAttr(ATTR_USER, node.attr(ATTR_USER))
            }
        }
    }
}