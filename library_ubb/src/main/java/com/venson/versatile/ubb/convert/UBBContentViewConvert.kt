package com.venson.versatile.ubb.convert

import android.content.Context
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.bean.ViewHolderType
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.span.ImageSpan
import com.venson.versatile.ubb.style.AbstractStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UBBContentViewConvert(
    context: Context,
    private val ubbContentAdapter: UBBContentAdapter
) : AbstractConvert(context) {

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

    override suspend fun insertSpan(content: String, span: Any) {
        super.insertSpan(content, span)
        if (span is ImageSpan || ubbContentAdapter.isCustomSpan(span)) {
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
                        it.type = ViewHolderType.VIEW_TEXT.type
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
                    var text = spannableString.subSequence(lastEnd, intRange.first)
                    if (text.length > 1 && text[text.length - 1] == '\n') {
                        text = text.subSequence(0, text.length - 1)
                    } else if (text.length == 1 && text[0] == '\n') {
                        text = ""
                    }
                    if (text.isNotEmpty()) {
                        ubbContentBeanList.add(
                            UBBContentBean().also {
                                it.text = text
                                it.type = ViewHolderType.VIEW_TEXT.type
                            }
                        )
                    }
                }
                val customStyle = when (any) {
                    is ISpan -> {
                        any.getStyle()
                    }
                    is AbstractStyle -> {
                        any
                    }
                    else -> {
                        null
                    }
                }
                if (customStyle != null) {
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.style = customStyle
                            it.type = customStyle.getHelper().getViewHolderType()
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
                        it.type = ViewHolderType.VIEW_TEXT.type
                    }
                )
            }
            return@withContext ubbContentBeanList
        }
    }
}