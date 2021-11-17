package com.venson.versatile.ubb.ext

import android.text.Editable
import android.text.SpannableString
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.utils.getTagNameBySpan

/**
 * 获取文本框的ubb代码
 */
fun Editable?.getUbb(): String {
    this ?: return ""
    val result = parseParagraph(this)
    return result.toString()
}

/**
 * 解析段落级
 */
private fun parseParagraph(charSequence: CharSequence): StringBuilder {
    val spannableString = SpannableString(charSequence)
    val result = StringBuilder()
    var next: Int
    var i = 0
    val spansArray = mutableListOf<Any>()
    while (i < spannableString.length) {
        /*
        下一次span变动的地方
         */
        next = spannableString.nextSpanTransition(
            i, spannableString.length, Any::class.java
        )
        /*
        当前部分包含的span
         */
        val spans = spannableString.getSpans(i, next, Any::class.java)

        if (spans.isNullOrEmpty()) {
            result.append(spannableString.substring(i, next))
            i = next
            continue
        }
        /*
        分析此段新增的span和保持的span
         */
        val newSpanList = mutableListOf<Any>()
        val newSpanMap = mutableMapOf<Any, Array<String>>()
        val oldSpanMap = mutableMapOf<Any, Array<String>>()
        spans.forEach { span ->
            val tagArray = getTagNameBySpan(span)
            if (tagArray.isNullOrEmpty()) {
                return@forEach
            }
            if (spansArray.contains(span)) {
                oldSpanMap[span] = tagArray
            } else {
                newSpanList.add(span)
                newSpanMap[span] = tagArray
            }
        }
        /*
        分析此段结束的span
         */
        if (spansArray.isNotEmpty()) {
            val spansReverseArray = spansArray.reversed()
            spansReverseArray.forEach { span ->
                if (oldSpanMap.containsKey(span)) {
                    return@forEach
                }
                getTagNameBySpan(span)?.let {
                    if (it.size == 2) {
                        result.append(it[1])
                    }
                }
                spansArray.remove(span)
            }
        }
        /*
        增加此段的新增span
         */
        newSpanList.sortBy { span ->
            spannableString.getSpanEnd(span)
        }
        newSpanList.forEach { span ->
            if (span is ISpan) {
                result.append(span.getUBB())
                return@forEach
            }
            newSpanMap[span]?.let { tagArray ->
                if (tagArray.size == 2) {
                    result.append(tagArray[0])
                    result.append(spannableString.substring(i, next))
                    spansArray.add(span)
                }
            }
        }
        i = next
    }
    if (spansArray.isNotEmpty()) {
        spansArray.reversed().forEach { span ->
            getTagNameBySpan(span)?.let {
                if (it.size == 2) {
                    result.append(it[1])
                }
            }
        }
    }
    return result
}