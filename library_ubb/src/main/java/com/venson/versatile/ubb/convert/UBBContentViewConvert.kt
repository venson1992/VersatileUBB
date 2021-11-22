package com.venson.versatile.ubb.convert

import android.graphics.Paint
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.span.ImageSpan
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class UBBContentViewConvert : UBBSpannableStringConvert() {

    private val mMediaSpanList = mutableListOf<Any>()
    private val mMediaSpanRangeList = mutableListOf<IntRange>()

    override fun resetContent() {
        super.resetContent()
        mMediaSpanList.clear()
        mMediaSpanRangeList.clear()
    }

    override fun onSpanParsed(
        span: Any?,
        start: Int,
        end: Int,
        content: String?,
        align: Paint.Align
    ) {
        super.onSpanParsed(span, start, end, content, align)
        span?.let {
            if (span is ImageSpan) {
                mMediaSpanList.add(span)
                mMediaSpanRangeList.add(IntRange(start, end))
            }
        }
    }

    override fun onStyleParsed(style: AbstractStyle, start: Int, end: Int, align: Paint.Align) {
        super.onStyleParsed(style, start, end, align)
        if (style is ImageStyle) {
            mMediaSpanList.add(style)
            mMediaSpanRangeList.add(IntRange(start, end))
        }
    }

    suspend fun getUBBContentBeanList(): List<UBBContentBean> {
        return withContext(Dispatchers.IO) {
            val spannableString = getSpannableString()
            if (spannableString.isEmpty()) {
                return@withContext emptyList()
            }
            val ubbContentBeanList = mutableListOf<UBBContentBean>()
            /*
            纯文本
             */
            if (mMediaSpanList.isNullOrEmpty()) {
                ubbContentBeanList.add(
                    UBBContentBean().also {
                        it.text = spannableString
                        it.type = UBBContentAdapter.TYPE_TEXT
                    }
                )
            }
            /*
            媒体分割
             */
            var lastIntRange: IntRange? = null
            mMediaSpanList.forEachIndexed { index, any ->
                val intRange = try {
                    mMediaSpanRangeList[index]
                } catch (e: Exception) {
                    return@forEachIndexed
                }
                val lastEnd = lastIntRange?.last ?: 0
                if (lastEnd < intRange.first) {
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.text = spannableString.subSequence(lastEnd, intRange.first)
                            it.type = UBBContentAdapter.TYPE_TEXT
                        }
                    )
                }
                if (any is ImageSpan) {
                    any.getStyle()
                } else if (any is ImageStyle) {
                    any
                } else {
                    null
                }?.let { style ->
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.style = style
                            it.type = UBBContentAdapter.TYPE_IMAGE
                        }
                    )
                }
                lastIntRange = intRange
            }
            /*
            媒体分割后最后一段文本
             */
            val lastEnd = lastIntRange?.last ?: spannableString.length
            if (lastEnd < spannableString.length) {
                ubbContentBeanList.add(
                    UBBContentBean().also {
                        it.text = spannableString.subSequence(lastEnd, spannableString.length)
                        it.type = UBBContentAdapter.TYPE_TEXT
                    }
                )
            }
            return@withContext ubbContentBeanList
        }
    }
}