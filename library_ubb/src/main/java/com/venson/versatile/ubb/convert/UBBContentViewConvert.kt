package com.venson.versatile.ubb.convert

import android.content.Context
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.span.ImageSpan
import com.venson.versatile.ubb.style.AbstractStyle
import com.venson.versatile.ubb.style.ImageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class UBBContentViewConvert(context: Context) : AbstractConvert(context) {

    private val mMediaSpanList = mutableListOf<Any>()
    private val mMediaSpanRangeList = mutableListOf<IntRange>()

    override fun isOnlyText(): Boolean {
        return true
    }

    override fun onParseStart() {
        mMediaSpanList.clear()
        mMediaSpanRangeList.clear()
    }

    override fun onParseComplete() {

    }

    override suspend fun onInsertStyle(customStyle: AbstractStyle) {
        super.onInsertStyle(customStyle)
        if (customStyle is ImageStyle) {

        }
    }

    override suspend fun insertSpan(content: String, span: Any) {
        super.insertSpan(content, span)
        if (span is ImageSpan) {
            mMediaSpanList.add(span)
            val end = getSpannableString().length
            mMediaSpanRangeList.add(IntRange(end - content.length, end))
        }
    }

    suspend fun parseUBB4SpannableStringBuilder(ubb: String?) {
        super.parseUBB(ubb)
        parseHandle()
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
                val imageStyle = when (any) {
                    is ImageSpan -> {
                        any.getStyle()
                    }
                    is ImageStyle -> {
                        any
                    }
                    else -> {
                        null
                    }
                }
                if (imageStyle != null) {
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.style = imageStyle
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