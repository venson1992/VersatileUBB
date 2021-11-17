package com.venson.versatile.ubb.convert

import android.graphics.Paint
import android.text.Layout
import android.text.style.AlignmentSpan
import com.venson.versatile.ubb.ext.getText
import com.venson.versatile.ubb.span.BaseSpan
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.utils.getSpanByTag
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.util.regex.Pattern

abstract class UBBConvert {

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
        val html = convertHTMLTag(ubb)
        val bodyElement = Jsoup.parseBodyFragment(html).body()
        onBodyElementCreated(bodyElement)
        println("html=${bodyElement.html()}")
        parseChildElement(bodyElement)
    }

    abstract fun resetContent()

    open fun convertHTML(ubb: String): String {
        return ubb
    }

    /**
     * 解析的document已经生成
     */
    fun onBodyElementCreated(bodyElement: Element?) {
        mContent = ""
        mImageList.clear()
        bodyElement ?: return
        /*
        文本
         */
        mContent = bodyElement.text()
        val ignoredTagList = getIgnoredConvert2Text().toMutableList()
        if (!ignoredTagList.contains("img")) {
            ignoredTagList.add("img")
        }
        ignoredTagList.forEach { ignoredTag ->
            bodyElement.getElementsByTag(ignoredTag).forEach { ignoredElement ->
                mContent = mContent.replace(ignoredElement.text(), "")
                /*
                记录图片
                 */
                if (ignoredTag == "img") {
                    mImageList.add(ignoredElement.attr("src"))
                }
            }
        }
    }

    fun getContent(): String = mContent

    fun getImageList(): List<String> = mImageList

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
    open fun getSpan(tagName: String, node: Node): BaseSpan? {
        return null
    }

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
            val span = getSpanByTag(tagName, childNode) ?: getSpan(tagName, childNode)
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
            if (tagName.equals("br", true)) {
                content = "\n"
                contentLength = content.length
            } else if (span is ISpan || childSize <= 0) {
                val text = childNode.getText()
                if (text.isEmpty()) {
                    return@forEachIndexed
                }
                content = text
                contentLength = text.length
            } else {
                val nowAlign = if (span is AlignmentSpan.Standard) {
                    when (span.alignment) {
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
            onSpanParsed(span, start, end, content, align)
        }
        return totalLength
    }

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
            dest, "table(.*?)", "table", "<table>", "</table>"
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
            "img",
            "img",
            "<img width=90%; height=auto; alt=\"\" src=\"", "\"></img>"
        )
        dest = convertHTML(
            dest,
            "img=,(.+?)",
            "img",
            "<img width=90%; height=auto; alt=\"\" src=\"",
            "\"></img>"
        )
        dest = convertHTML(
            dest,
            "size=(.+?)",
            "size",
            "<span style=\"font-size:$2; color:black\">", "</span>"
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

    /**
     * 转换文本中对应标签
     *
     * @param source     原文本
     * @param oldTag   被替换标签
     * @param tagStart 新标签开始
     * @param tagEnd   新标签结束
     * @return 替换后文本
     */
    fun convertHTML(source: String?, oldTag: String, tagStart: String, tagEnd: String): String {
        return convertHTML(source, oldTag, oldTag, tagStart, tagEnd)
    }

    /**
     * 转换文本中对应标签
     *
     * @param source 替换前文本
     * @param oldTagStart 被替换标签开始
     * @param oldTagEnd 被替换标签结束
     * @param newTagStart 新标签开始
     * @param newTagEnd 新标签结束
     * @return 替换后文本
     */
    fun convertHTML(
        source: String?,
        oldTagStart: String,
        oldTagEnd: String,
        newTagStart: String,
        newTagEnd: String
    ): String {
        var dest = source ?: return ""
        try {
            val matcher = Pattern.compile(
                "(\\[$oldTagStart\\])",
                Pattern.DOTALL or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE
            )
            dest = matcher.matcher(dest).replaceAll(newTagStart)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val matcher = Pattern.compile(
                "(\\[/$oldTagEnd\\])",
                Pattern.DOTALL or Pattern.CASE_INSENSITIVE or Pattern.MULTILINE
            )
            dest = matcher.matcher(dest).replaceAll(newTagEnd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dest
    }
}