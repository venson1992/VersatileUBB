package com.venson.versatile.ubb.convert

import android.content.Context
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.span.GlideImageSpan
import com.venson.versatile.ubb.span.ISpan
import com.venson.versatile.ubb.style.AbstractStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UBBContentViewConvert(context: Context) : AbstractConvert(context) {

    private val mMediaSpanList = mutableListOf<Any>()
    private val mMediaSpanRangeList = mutableListOf<IntRange>()
    private val mDefaultType = UBB.getViewType(DefaultViewHolder::class.java)

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
        if (span is ISpan) {
            val type = UBB.getProvider(span.getTagName())
                ?.let { provider ->
                    UBB.getViewType(provider)
                }
                ?: let {
                    mDefaultType
                }
            if (span is GlideImageSpan || type != mDefaultType) {
                mMediaSpanList.add(span)
                val end = getSpannableString().length
                mMediaSpanRangeList.add(IntRange(end - content.length, end))
            }
        }
    }

    suspend fun parseUBB4SpannableStringBuilder(ubb: String?) {
        super.parseUBB(ubb)
        parseHandle()
    }

    internal suspend fun getUBBContentBeanList(): List<UBBContentBean> {
        return withContext(Dispatchers.IO) {
            val spannableString = getSpannableString()
            if (spannableString.isEmpty()) {
                return@withContext emptyList()
            }
            val ubbContentBeanList = mutableListOf<UBBContentBean>()
            /*
            纯文本
             */
            if (mMediaSpanList.isEmpty()) {
                ubbContentBeanList.add(
                    UBBContentBean().also {
                        it.text = spannableString
                        it.type = mDefaultType
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
                    val text = getSplitText(lastEnd, intRange.first, true)
                    if (text.isNotEmpty()) {
                        ubbContentBeanList.add(
                            UBBContentBean().also {
                                it.text = text
                                it.type = mDefaultType
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
                            val type = UBB.getProvider(customStyle.getTagName())
                                ?.let { provider ->
                                    UBB.getViewType(provider)
                                }
                                ?: let {
                                    mDefaultType
                                }
                            it.type = type
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
                val text = getSplitText(lastEnd, spannableString.length, false)
                if (text.isNotEmpty()) {
                    ubbContentBeanList.add(
                        UBBContentBean().also {
                            it.text = text
                            it.type = mDefaultType
                        }
                    )
                }
            }
            return@withContext ubbContentBeanList
        }
    }

    private fun getSplitText(start: Int, end: Int, isRemoveLastBreakLine: Boolean): CharSequence {
        val spannableString = getSpannableString()
        var text = spannableString.subSequence(start, end)
        if (isRemoveLastBreakLine) {
            if (text.length > 1 && text[text.length - 1] == UBB.BREAK_LINE) {
                text = text.subSequence(0, text.length - 1)
            } else if (text.length == 1 && text[0] == UBB.BREAK_LINE) {
                text = ""
            }
        }
        if (text.startsWith(UBB.BREAK_LINE)) {
            text = text.subSequence(1, text.length)
        }
        return text
    }
}