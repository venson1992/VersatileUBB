package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.Layout
import android.text.style.AlignmentSpan
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.ext.getText
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.AtSomeoneStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.utils.convertHTML
import com.venson.versatile.ubb.utils.getSpanByTag
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

abstract class AbstractConvert {

    companion object {
        private const val TAG = "UBBConvert"
    }

    //预览文本内容，不包含图片和其他排除掉的标签内容
    private var mContent: String = ""

    //图片路径列表
    private var mImageList: MutableList<String> = mutableListOf()

    /**
     * 解析ubb方法
     */
    fun parseUBB(ubb: String?) {
        resetContent()
        if (ubb.isNullOrEmpty()) {
            return
        }
        /*
        自带的标签解析
         */
        UBB.registerStyleBuilder(AtSomeoneStyle.Helper)
        UBB.registerStyleBuilder(ImageStyle.Helper)
        /*
        转换html
         */
        val html = convertHTMLTag(ubb)
        val bodyElement = Jsoup.parseBodyFragment(html).body()
        /*
        解析
         */
        onBodyElementCreated(bodyElement)
        UBB.logV(TAG, "html=${bodyElement.html()}")
        parseChildElement(bodyElement)
    }

    abstract fun resetContent()

    /**
     * 解析的document已经生成
     */
    private fun onBodyElementCreated(bodyElement: Element?) {
        mContent = ""
        mImageList.clear()
        bodyElement ?: return
        /*
        文本
         */
        mContent = bodyElement.text()
        val imageTag = ImageStyle.Helper.getTagName()
        val ignoredTagList = getIgnoredConvert2Text().toMutableList()
        if (!ignoredTagList.contains(imageTag)) {
            ignoredTagList.add(imageTag)
        }
        ignoredTagList.forEach { ignoredTag ->
            bodyElement.getElementsByTag(ignoredTag).forEach { ignoredElement ->
                mContent = mContent.replace(ignoredElement.text(), "")
                /*
                记录图片
                 */
                if (ignoredTag == imageTag) {
                    mImageList.add(ignoredElement.attr(ImageStyle.ATTR_SRC))
                }
            }
        }
    }

    fun getContent(): String = mContent

    fun getImageList(): List<String> = mImageList

    /**
     * 传入忽略文本的标签
     * @return 指定这些标签的文本不显示成文本
     */
    fun getIgnoredConvert2Text(): List<String> = emptyList()

    abstract fun getContentLength(): Int

    /**
     * 解析样式
     */
    private fun parseChildElement(node: Node, align: Paint.Align = Paint.Align.LEFT): Int {
        var totalLength = 0
        node.childNodes().forEachIndexed { index, childNode ->
            val childSize = childNode.childNodeSize()
            val tagName = childNode.nodeName()
            val normalSpan = getSpanByTag(tagName, childNode)
            val style = getStyle(tagName, childNode, align)
            /*
            如果p兄弟tag不是br，则追加br
             */
            var isNeedAddBreakLine = false
            if (tagName.equals("p", true)) {
                try {
                    if (index > 0) {
                        val preTagName = node.childNode(index - 1).nodeName()
                        if (!preTagName.equals("br", true)) {
                            isNeedAddBreakLine = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            var start = getContentLength()
            if (isNeedAddBreakLine) {
                val breakLineText = "\n"
                onSpanParsed(
                    null,
                    start,
                    start + breakLineText.length,
                    breakLineText,
                    align
                )
                start += breakLineText.length
                totalLength += breakLineText.length
            }
            val content: String
            val contentLength: Int
            if (tagName.equals("br", true)
                || (childSize <= 0 && tagName.equals("p", true))
            ) {
                content = "\n"
                contentLength = content.length
            } else if (normalSpan == null || style != null || childSize <= 0) {
                val text = style?.getSpanText() ?: childNode.getText()
                if (text.isEmpty()) {
                    return@forEachIndexed
                }
                content = text
                contentLength = text.length
            } else {
                val nowAlign = if (normalSpan is AlignmentSpan.Standard) {
                    when (normalSpan.alignment) {
                        Layout.Alignment.ALIGN_CENTER -> {
                            Paint.Align.CENTER
                        }
                        Layout.Alignment.ALIGN_OPPOSITE -> {
                            Paint.Align.RIGHT
                        }
                        else -> {
                            Paint.Align.LEFT
                        }
                    }
                } else {
                    align
                }
                content = ""
                contentLength = parseChildElement(childNode, nowAlign)
            }
            totalLength += contentLength
            val end = start + contentLength
            if (style != null) {
                onStyleParsed(style, start, end, align)
            } else {
                onSpanParsed(normalSpan, start, end, content, align)
            }
        }
        return totalLength
    }

    /**
     * 非自定义span
     */
    abstract fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String? = null,
        align: Paint.Align
    )

    /**
     * 返回一些自定义标签span
     */
    private fun getStyle(tagName: String, node: Node, align: Paint.Align): AbstractStyle? {
        UBB.getStyleBuilderMap()[tagName]?.let { builder ->
            return builder.fromUBB(node, align)
        }
        return null
    }

    /**
     * 自定义span
     */
    abstract fun onStyleParsed(
        style: AbstractStyle,
        start: Int,
        end: Int,
        align: Paint.Align
    )

    /**
     * 替换成html
     */
    private fun convertHTMLTag(ubb: String): String {
        var dest = convertHTML(ubb)
        /*
        无参标签
         */
        dest = convertHTML(dest, "br", "<br>", "</br>")
        dest = convertHTML(dest, "\\n", "<br>", "</br>")
        dest = convertHTML(dest, "u", "<u>", "</u>")
        dest = convertHTML(dest, "u", "<u>", "</u>")
        dest = convertHTML(dest, "i", "<i>", "</i>")
        dest = convertHTML(dest, "li", "<li>", "</li>")
        dest = convertHTML(dest, "list=(.*?)", "<ol>", "</ol>")
        dest = convertHTML(dest, "list(.*?)", "<ul>", "</ul>")
        dest = convertHTML(dest, "\\*", "<li>", "</li>")
        dest = convertHTML(dest, "ol", "<ol>", "</ol>")
        dest = convertHTML(dest, "ul", "<ul>", "</ul>")
        dest = convertHTML(dest, "b", "<b>", "</b>")
        dest = convertHTML(dest, "h1", "<h1>", "</h1>")
        dest = convertHTML(dest, "h2", "<h2>", "</h2>")
        dest = convertHTML(dest, "h3", "<h3>", "</h3>")
        dest = convertHTML(dest, "h4", "<h4>", "</h4>")
        dest = convertHTML(dest, "h5", "<h5>", "</h5>")
        dest = convertHTML(dest, "h6", "<h6>", "</h6>")
        dest = convertHTML(dest, "s", "<s>", "</s>")
        /*
        含参数标签
        */
        dest = convertHTML(
            dest, "tr(.*?)", "tr", "<tr>", "</tr>"
        )
        dest = convertHTML(
            dest, "td(.*?)", "td", "<td>", "</td>"
        )
        dest = convertHTML(
            dest,
            "table(.*?)",
            "table",
            "<table>",
            "</table>"
        )
        dest = convertHTML(
            dest,
            "align=(.*?)",
            "align",
            "<p align=\"$2\">",
            "</p>"
        )
        dest = convertHTML(
            dest,
            "url=(.*?)",
            "url",
            "<a href=\"$2\" target=_blank>",
            "</a>"
        )
        dest = convertHTML(
            dest,
            "url(.*?)",
            "url",
            "<a $2 target=_blank>",
            "</a>"
        )
        dest = convertHTML(
            dest,
            "size=(.+?)",
            "size",
            "<span style=\"font-size:$2; color:black\">",
            "</span>"
        )
        dest = convertHTML(
            dest,
            "font=(.+?)",
            "font",
            "<font face=\"$2\" color=\"black\">",
            "</font>"
        )
        dest = convertHTML(
            dest,
            "color=(.+?)",
            "color",
            "<font color=\"$2\">",
            "</font>"
        )
        dest = convertHTML(
            dest,
            "email=(.+?)",
            "email",
            "<a href=\"mailto:$2\">",
            "</a>"
        )
        dest = convertHTML(
            dest,
            "back=(.*?)",
            "back",
            "<span style=\"background-color:$2\">",
            "</span>"
        )
        return dest
    }

    private fun convertHTML(ubb: String): String {
        var dest = ubb
        UBB.getStyleBuilderMap().forEach { entry ->
            dest = entry.value.convertUBB(dest)
        }
        return dest
    }
}