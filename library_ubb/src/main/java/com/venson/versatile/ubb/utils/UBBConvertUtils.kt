package com.venson.versatile.ubb.utils

import java.util.regex.Pattern

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