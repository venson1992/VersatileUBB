package com.venson.versatile.ubb.convert

import android.graphics.Paint
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.span.ImageSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class UBBContentViewUBBConvert : SpannableStringUBBConvert() {

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

    suspend fun getUBBContentBeanList(): List<UBBContentBean> {
        return withContext(Dispatchers.IO) {
            val spannableString = getSpannableString()
            if (spannableString.isEmpty()) {
                return@withContext emptyList()
            }
            val ubbContentBeanList = mutableListOf<UBBContentBean>()
            if (mMediaSpanList.isNullOrEmpty()) {
                ubbContentBeanList.add(
                    UBBContentBean().also {
                        it.text = spannableString
                        it.type = UBBContentAdapter.TYPE_TEXT
                    }
                )
            }
            var lastIntRange: IntRange? = null
            mMediaSpanList.forEachIndexed { index, any ->
                val intRange = try {
                    mMediaSpanRangeList[index]
                } catch (e: Exception) {
                    return@forEachIndexed
                }
                val lastEnd = lastIntRange?.last ?: intRange.first
                if (lastEnd < intRange.first) {
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.text = spannableString.substring(lastEnd, intRange.first)
                            it.type = UBBContentAdapter.TYPE_TEXT
                        }
                    )
                }
                if (any is ImageSpan) {
                    any.getSpanContent()
                } else {
                    null
                }?.let { spanContent ->
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.spanContent = spanContent
                            it.type = UBBContentAdapter.TYPE_IMAGE
                        }
                    )
                }
                lastIntRange = intRange
            }
            return@withContext ubbContentBeanList
        }
    }
}