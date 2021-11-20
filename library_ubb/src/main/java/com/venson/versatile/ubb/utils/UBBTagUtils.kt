package com.venson.versatile.ubb.utils

import android.graphics.Typeface
import android.text.Layout
import android.text.style.AlignmentSpan
import android.text.style.BulletSpan
import android.text.style.QuoteSpan
import android.text.style.StyleSpan
import com.venson.versatile.ubb.span.ISpan
import org.jsoup.nodes.Node

fun getSpanByTag(tagName: String, node: Node): Any? {
    if (isSupportTag(tagName)) {
        if (tagName.equals("p", true)) {
            val align = node.attr("align")
            return AlignmentSpan.Standard(
                when {
                    "center".equals(align, true) -> {
                        Layout.Alignment.ALIGN_CENTER
                    }
                    "opposite".equals(align, true) -> {
                        Layout.Alignment.ALIGN_OPPOSITE
                    }
                    else -> {
                        Layout.Alignment.ALIGN_NORMAL
                    }
                }
            )
        }
        if (tagName.equals("blockquote", true)) {
            return QuoteSpan()
        }
        if (tagName.equals("bullet", true)) {
            return BulletSpan()
        }
        if (tagName.equals("b", true)) {
            return StyleSpan(Typeface.BOLD)
        }
        if (tagName.equals("i", true)) {
            return StyleSpan(Typeface.ITALIC)
        }
    }
    return null
}

/**
 * 判断传入的tag是否支持转换
 */
fun isSupportTag(tag: String): Boolean {
    if (tag.equals("blockquote", true)) {
        return true
    }
    if (tag.equals("p", true)) {
        return true
    }
    if (tag.equals("bullet", true)) {
        return true
    }
    if (tag.equals("b", true)) {
        return true
    }
    if (tag.equals("i", true)) {
        return true
    }
    return false
}

/**
 * 根据传入的
 */
fun getTagNameBySpan(span: Any?): Array<String>? {
    span ?: return null
    /*
    段落级Span
    QuoteSpan影响段落层次的文本格式。它可以给一个段落加上垂直的引用线。
     */
    if (span is QuoteSpan) {
        return arrayOf("[blockquote]", "[/blockquote]")
    }
    /*
    段落级Span
    AlignmentSpan.Standard影响段落层次的文本格式。它可以把段落的每一行文本按正常、居中、相反的方式对齐。
     */
    if (span is AlignmentSpan.Standard) {
        return arrayOf(
            when (span.alignment) {
                Layout.Alignment.ALIGN_CENTER -> {
                    "align=center"
                }
                Layout.Alignment.ALIGN_OPPOSITE -> {
                    "align=opposite"
                }
                else -> {
                    "align=left"
                }
            },
            "[/align]"
        )
    }
    /*
    段落级Span
    BulletSpan影响段落层次的文本格式。它可以给段落的开始处加上项目符号。
    gapWidth:项目符号和文本之间的间隙
    color: 项目符号的颜色，默认为透明
     */
    if (span is BulletSpan) {
        return arrayOf("[bullet]", "[/bullet]")
    }
    /*
    字符级
     */
    if (span is StyleSpan) {
        if (span.style == Typeface.BOLD_ITALIC || span.style == Typeface.BOLD) {
            return arrayOf("[b]", "[/b]")
        }
        if (span == Typeface.ITALIC) {
            return arrayOf("[i]", "[/i]")
        }
        return arrayOf("", "")
    }
    if (span is ISpan) {
        return arrayOf("[${span.getTag()}]", "[/${span.getTag()}]")
    }
    return null
}