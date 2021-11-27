package com.venson.versatile.ubb.convert

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.text.Html
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.bean.ViewHolderType
import com.venson.versatile.ubb.ext.getText
import com.venson.versatile.ubb.ext.isEndBreakLine
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.AtSomeoneStyle
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.utils.convertHTML
import com.venson.versatile.ubb.utils.getSpanByTag
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

/**
 * ubb解析类
 */
abstract class AbstractConvert(val context: Context) {

    companion object {
        private const val TAG = "UBBConvert"
    }

    private var mBodyElement: Node? = null

    private val mSpannableStringBuilder = SpannableStringBuilder()

    private var mLastISpanIndex: Int = -1

    //预览文本内容，不包含图片和其他排除掉的标签内容
    private var mContent: String = ""

    //图片列表
    private var mImageList: MutableList<String> = mutableListOf()

    //音频列表
    private var mAudioList: MutableList<String> = mutableListOf()

    //视频列表
    private var mVideoList: MutableList<String> = mutableListOf()

    /**
     * 解析ubb方法
     */
    fun parseUBB(ubb: String?) {
        mContent = ""
        mImageList.clear()
        mAudioList.clear()
        mVideoList.clear()
        mSpannableStringBuilder.clear()
        onParseStart()
        if (ubb.isNullOrEmpty()) {
            onParseComplete()
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
        val bodyElement = Jsoup.parseBodyFragment(html).body().also {
            mBodyElement = it
        }
        /*
        解析
         */
        onBodyElementCreated(bodyElement)
        UBB.logV(TAG, "html=${bodyElement.html()}")
        if (isOnlyText()) {
            mSpannableStringBuilder.append(getContent())
            onParseComplete()
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            parseHandle()
        }
    }

    protected suspend fun parseHandle(): SpannableStringBuilder {
        mSpannableStringBuilder.clear()
        val bodyElement = mBodyElement ?: let {
            onParseComplete()
            return mSpannableStringBuilder
        }
        return withContext(Dispatchers.IO) {
            parseNode(bodyElement)
            withContext(Dispatchers.Main) {
                onParseComplete()
            }
            return@withContext mSpannableStringBuilder
        }
    }

    abstract fun isOnlyText(): Boolean

    abstract fun onParseStart()

    /**
     * 解析的document已经生成
     */
    private fun onBodyElementCreated(bodyElement: Element?) {
        bodyElement ?: return
        /*
        文本
         */
        mContent = bodyElement.text()
        /*
        过滤媒体
         */
        val ignoredTagList = getIgnoredConvert2Text().toMutableList()
        ViewHolderType.values().forEach { mediaTagType ->
            if (!ignoredTagList.contains(mediaTagType.tagName)) {
                ignoredTagList.add(mediaTagType.tagName)
            }
        }
        ignoredTagList.forEach { ignoredTag ->
            bodyElement.getElementsByTag(ignoredTag).forEach { ignoredElement ->
                val htmlCode = ignoredElement.toString()
                mContent = mContent.replace(ignoredElement.text(), "")
                if (ignoredTag == ViewHolderType.VIEW_IMAGE.tagName) {
                    mImageList.add(htmlCode)
                }
                if (ignoredTag == ViewHolderType.VIEW_AUDIO.tagName) {
                    mAudioList.add(htmlCode)
                }
                if (ignoredTag == ViewHolderType.VIEW_VIDEO.tagName) {
                    mVideoList.add(htmlCode)
                }
            }
        }
    }

    fun getSpannableString(): SpannableStringBuilder = mSpannableStringBuilder

    fun getContent(): String = mContent

    fun getImageList(): List<String> = mImageList

    fun getAudioList(): List<String> = mAudioList

    fun getVideoList(): List<String> = mVideoList

    /**
     * 传入忽略文本的标签
     * @return 指定这些标签的文本不显示成文本
     */
    fun getIgnoredConvert2Text(): List<String> = emptyList()

    fun getContentLength(): Int {
        return mSpannableStringBuilder.length
    }

    /**
     * 解析node
     */
    private suspend fun parseNode(node: Node, align: Paint.Align = Paint.Align.LEFT) {
        withContext(Dispatchers.IO) {
            val nodeName = node.nodeName()
            val htmlCode = node.toString()
            /*
            文本样式
             */
            if (nodeName.equals("span", true)
                || nodeName.equals("font", true)
            ) {
                var htmlSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(htmlCode, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    Html.fromHtml(htmlCode)
                }
                parseSizeSpan(htmlSpanned, node)?.let {
                    htmlSpanned = it
                }
                mSpannableStringBuilder.append(htmlSpanned)
                return@withContext
            }
            val childNodeSize = node.childNodeSize()
            /*
            换行
             */
            if (nodeName.equals("br", true)) {
                mSpannableStringBuilder.append(UBB.BREAK_LINE)
                return@withContext
            }
            /*
            不处理
             */
            if (childNodeSize <= 0 && nodeName.equals("p", true)) {
                return@withContext
            }
            val normalSpan = getSpanByTag(nodeName, node)
            val customStyle = if (normalSpan != null) {
                null
            } else {
                getStyle(nodeName, node, align)
            }
            if (customStyle != null) {
                onInsertStyle(customStyle)
                return@withContext
            }
            if (childNodeSize <= 0) {
                if (normalSpan != null) {
                    insertSpan(node.getText(), normalSpan)
                    return@withContext
                }
                mSpannableStringBuilder.append(node.getText())
                return@withContext
            }
            /*
            子级标签适用的align样式
             */
            val nextAlign = if (normalSpan != null && normalSpan is AlignmentSpan.Standard) {
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
            val start = mSpannableStringBuilder.length
            /*
            子级标签
             */
            for (childIndex in 0 until childNodeSize) {
                val childNode = node.childNode(childIndex)
                val childNodeName = childNode.nodeName()
                if (childNodeName.equals("p", true) && childIndex > 0) {
                    val preTagName = node.childNode(childIndex - 1).nodeName()
                    if (!preTagName.equals("br", true)) {
                        withContext(Dispatchers.Main) {
                            mSpannableStringBuilder.append(UBB.BREAK_LINE)
                        }
                    }
                }
                parseNode(childNode, nextAlign)
            }
            val end = mSpannableStringBuilder.length
            mSpannableStringBuilder.setSpan(
                normalSpan,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    /**
     * 解析文本大小
     */
    private fun parseSizeSpan(htmlSpanned: Spanned, node: Node): SpannableStringBuilder? {
        var dest = SpannableStringBuilder(htmlSpanned)
        if (node.hasAttr("size")) {
            val text = node.getText()
            val start = htmlSpanned.indexOf(text)
            if (start < 0) {
                return null
            }
            val end = start + text.length
            val sizeValue = node.attr("size")
            var size: Int? = null
            var percent: Float? = null
            when {
                sizeValue.endsWith("px", true) -> {
                    size = sizeValue.substring(0, sizeValue.length - 2).toIntOrNull()
                }
                sizeValue.endsWith("%") -> {
                    percent = sizeValue.substring(0, sizeValue.length - 1).toIntOrNull()
                        ?.div(100F)
                }
                else -> {
                    size = sizeValue.toIntOrNull()
                }
            }
            var sizeSpan: MetricAffectingSpan? = null
            if (size != null) {
                sizeSpan = AbsoluteSizeSpan(size, true)
            }
            if (percent != null) {
                sizeSpan = RelativeSizeSpan(percent)
            }
            if (sizeSpan != null) {
                return dest.also {
                    it.setSpan(
                        sizeSpan,
                        start,
                        end,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            return null
        }
        node.childNodes().forEach { childNode ->
            if (childNode.nodeName().equals("font", true)) {
                parseSizeSpan(dest, childNode)?.let {
                    dest = it
                }
            }
        }
        return dest
    }

    protected open suspend fun onInsertStyle(customStyle: AbstractStyle) {
        if (customStyle is ImageStyle) {
            withContext(Dispatchers.IO) {
                customStyle.getImageSpan(context, 500)?.let { imageSpan ->
                    insertSpan(customStyle.getSpanText(), imageSpan)
                }
            }
            return
        }
        customStyle.getSpan()?.let { span ->
            insertSpan(customStyle.getSpanText(), span)
        }
    }

    protected open suspend fun insertSpan(content: String, span: Any) {
        withContext(Dispatchers.Main) {
            if (mSpannableStringBuilder.isNotEmpty()) {
                if (span is ISpan) {
                    if (span.isStartSingleLine()) {
                        if (!mSpannableStringBuilder.isEndBreakLine()) {
                            mSpannableStringBuilder.append(UBB.BREAK_LINE)
                        }
                    }
                } else {
                    if (mLastISpanIndex == mSpannableStringBuilder.length) {
                        mSpannableStringBuilder.append(UBB.BREAK_LINE)
                    }
                }
            }
            val start = mSpannableStringBuilder.length
            mSpannableStringBuilder.append(content)
            val end = mSpannableStringBuilder.length
            mSpannableStringBuilder.setSpan(
                span,
                start,
                end,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (span is ISpan) {
                if (span.isEndSingleLine()) {
                    if (!mSpannableStringBuilder.isEndBreakLine()) {
                        mLastISpanIndex = mSpannableStringBuilder.length
                    }
                }
            }
        }
    }

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
     * 替换成html
     */
    private fun convertHTMLTag(ubb: String): String {
        /*
        取消所有\n换行
         */
        var dest = convertHTML(ubb.replace(UBB.BREAK_LINE.toString(), ""))
        /*
        无参标签
         */
        dest = convertHTML(dest, "br", "<br>", "</br>")
//        dest = convertHTML(dest, "\\n", "<br>", "</br>")
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
            "<font size=\"$2\">",
            "</font>"
        )
        dest = convertHTML(
            dest,
            "font=(.+?)",
            "font",
            "<font face=\"$2\">",
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

    abstract fun onParseComplete()
}