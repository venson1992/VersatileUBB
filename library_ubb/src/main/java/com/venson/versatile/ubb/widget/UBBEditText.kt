package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.convert.UBBEditTextConvert
import com.venson.versatile.ubb.ext.getUbb
import com.venson.versatile.ubb.span.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 支持UBB自定义Span
 */
class UBBEditText : androidx.appcompat.widget.AppCompatEditText {

    private val logTag = this.javaClass.simpleName

    //是否开启文本监听
    private val isMonitoring = AtomicBoolean(false)

    //文本改动监听
    private val mTextWatcher by lazy {
        object : TextWatcher {

            var selectedReplacementSpans: Array<AbstractReplacementSpan>? = null
            var startPos = 0
            var endPos = 0

            var needDeleteSpanStart: Int? = null
            var needDeleteSpanEnd: Int? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                UBB.log(
                    logTag,
                    "beforeTextChanged:: s = " + s + ", start = " + start
                            + ", count = " + count + ", after = " + after
                )
                /*
                监听删除
                 */
                needDeleteSpanStart = null
                needDeleteSpanEnd = null
                if (count == 1 && !s.isNullOrEmpty() && after == 0) {
                    editableText.getSpans(
                        start, start + count, AbstractLineEnableSpan::class.java
                    )?.let { spans ->
                        if (spans.isNotEmpty()) {
                            spans[0]?.let { lineEnableSpan ->
                                val spanStart = editableText.getSpanStart(lineEnableSpan)
                                val spanEnd = editableText.getSpanEnd(lineEnableSpan)
                                if (start + count == spanEnd) {
                                    needDeleteSpanStart = spanStart
                                    needDeleteSpanEnd = spanEnd - count
                                }
                            }
                        }
                    }
                }
                /*
                监听span增删
                 */
                mSpanChangedListener?.let {
                    selectedReplacementSpans = null
                    val editable = text
                    if (editable != null) {
                        selectedReplacementSpans = editable.getSpans(
                            start, start + count, AbstractReplacementSpan::class.java
                        )
                    }
                    UBB.log(
                        logTag,
                        "beforeTextChanged::selectedAreTagSpans="
                                + Arrays.toString(selectedReplacementSpans)
                    )
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                UBB.log(
                    logTag,
                    "onTextChanged:: s = " + s + ", start = " + start
                            + ", count = " + count + ", before = " + before
                )
                startPos = start
                endPos = start + count
            }

            override fun afterTextChanged(s: Editable?) {
                if (UBB.logEnable()) {
                    Log.d(logTag, "afterTextChanged:: s = $s")
                }
                /*
                监听span整块删除
                 */
                if (needDeleteSpanStart != null && needDeleteSpanEnd != null) {
                    editableText.delete(needDeleteSpanStart!!, needDeleteSpanEnd!!)
                    return
                }
                /*
                监听span插入和移除
                 */
                mSpanChangedListener?.let { changedListener ->
                    if (endPos <= startPos) {
                        UBB.log(logTag, "删除: start == $startPos endPos == $endPos")
                        selectedReplacementSpans?.let { spans ->
                            changedListener.onChanged(spans, false)
                        }
                    } else {
                        UBB.log(logTag, "增加: start == $startPos endPos == $endPos")
                        selectedReplacementSpans =
                            s?.getSpans(startPos, endPos, AbstractReplacementSpan::class.java)
                        selectedReplacementSpans?.let { spans ->
                            changedListener.onChanged(spans, true)
                        }
                    }
                }
                /*
                前面span是否需要后置另起一行
                 */
                if (startPos > 0) {
                    try {
                        s?.getSpans(startPos - 1, startPos, ISpan::class.java)?.let { spans ->
                            var isBreakLine = false
                            if (spans.isNotEmpty()) {
                                for (index in spans.indices) {
                                    if (spans[index].isEndSingleLine()) {
                                        isBreakLine = true
                                        break
                                    }
                                }
                            }
                            if (isBreakLine
                                && s.length > startPos
                                && s[startPos] != UBB.BREAK_LINE
                            ) {
                                s.insert(startPos, UBB.BREAK_LINE.toString())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                /*
                后面span是否需要前置另起一行
                 */
                if (endPos > 0) {
                    try {
                        s?.getSpans(endPos, endPos + 1, ISpan::class.java)?.let { spans ->
                            var isBreakLine = false
                            if (spans.isNotEmpty()) {
                                for (index in spans.indices) {
                                    if (spans[index].isStartSingleLine()) {
                                        isBreakLine = true
                                        break
                                    }
                                }
                            }
                            if (isBreakLine
                                && s.length > startPos
                                && s[endPos - 1] != UBB.BREAK_LINE
                            ) {
                                s.insert(endPos, UBB.BREAK_LINE.toString())
                                setSelection(endPos - 1)//移动光标位置
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    /*
    加粗或斜体样式过滤器
     */
    private val mBoldAndItalicStyleFilter = InputFilter { source: CharSequence?,
                                                          start: Int,
                                                          end: Int,
                                                          dest: Spanned?,
                                                          dStart: Int,
                                                          dEnd: Int ->
        if (source.isNullOrEmpty() || source == text) {
            return@InputFilter null
        }
        /*
        下划线样式不做处理
         */
        if (!SpannedString(source).getSpans(start, end, UnderlineSpan::class.java)
                .isNullOrEmpty()
        ) {
            return@InputFilter null
        }
        return@InputFilter SpannableStringBuilder(source).apply {
            /*
            是否包含自定义span，有则原样返回，不改变样式
             */
            val spans = getSpans(0, length, AbstractReplacementSpan::class.java)
            if (!spans.isNullOrEmpty()) {
                return@InputFilter null
            }
            /*
            移除已有的StyleSpan
             */
            getSpans(0, length, StyleSpan::class.java)?.forEach { styleSpan ->
                removeSpan(styleSpan)
            }
            /*
            设置统一的StyleSpan
             */
            setSpan(
                StyleSpan(
                    if (mBoldEnable && mItalicEnable) {
                        Typeface.BOLD_ITALIC
                    } else if (mBoldEnable) {
                        Typeface.BOLD
                    } else if (mItalicEnable) {
                        Typeface.ITALIC
                    } else {
                        Typeface.NORMAL
                    }
                ),
                0,
                length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private var mLengthFilterSize: Int? = null

    //span插入、移除的监听器
    var mSpanChangedListener: OnSpanChangedListener? = null

    //span的选中状态
    var mSpanSelectionListener: OnSpanSelectionListener? = null

    //是否添加加粗样式
    var mBoldEnable: Boolean = false

    //是否添加斜体样式
    var mItalicEnable: Boolean = false

    //记录上次触发onSelectionChanged的坐标，相同不再触发
    private var recordSelStart = 0
    private var recordSelEnd = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        androidx.appcompat.R.attr.editTextStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startMonitoring()
        updateFilters()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopMonitoring()
    }

    /**
     * 开始文本改动监听
     */
    private fun startMonitoring() {
        if (isMonitoring.get()) {
            return
        }
        removeTextChangedListener(mTextWatcher)
        addTextChangedListener(mTextWatcher)
        isMonitoring.set(true)
    }

    /**
     * 停止文本改动监听
     */
    private fun stopMonitoring() {
        if (isMonitoring.get()) {
            removeTextChangedListener(mTextWatcher)
            isMonitoring.set(false)
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (selStart == recordSelStart && selEnd == recordSelEnd) {
            return
        }
        val lineSpans = editableText.getSpans(selStart, selEnd, AbstractLineEnableSpan::class.java)
        if (!lineSpans.isNullOrEmpty()) {
            var startIndex = -1
            var endIndex = -1
            lineSpans.forEachIndexed { index, baseLineEnableSpan ->
                val tempStartIndex = editableText.getSpanStart(baseLineEnableSpan)
                val tempEndIndex = editableText.getSpanEnd(baseLineEnableSpan)
                if (selEnd > tempStartIndex) {
                    if (index == 0) {
                        startIndex = tempStartIndex
                        endIndex = tempEndIndex
                    } else {
                        if (tempStartIndex < startIndex) {
                            startIndex = tempStartIndex
                        }
                        if (tempEndIndex > endIndex) {
                            endIndex = tempEndIndex
                        }
                    }
                }
            }
            if (startIndex != -1 && endIndex != -1) {
                if (selStart == selEnd) {
                    recordSelStart = endIndex
                    recordSelEnd = endIndex
                    setSelection(endIndex, endIndex)
                } else {
                    recordSelStart = startIndex
                    recordSelEnd = endIndex
                    setSelection(startIndex, endIndex)
                }
                return
            }
        }
        val spans = if (selStart == selEnd && selStart > 0) {
            editableText.getSpans(selStart - 1, selEnd, StyleSpan::class.java)
        } else {
            editableText.getSpans(selStart, selEnd, StyleSpan::class.java)
        }
        var hasBoldStyle = false
        var hasItalicStyle = false
        if (!spans.isNullOrEmpty()) {
            for (index in 0 until spans.count()) {
                val span = spans[index]
                val style = span.style
                if (style or Typeface.BOLD_ITALIC == style) {
                    hasBoldStyle = true
                    hasItalicStyle = true
                    break
                }
                if (style or Typeface.BOLD == style) {
                    hasBoldStyle = true
                }
                if (style or Typeface.ITALIC == style) {
                    hasItalicStyle = true
                }
            }
        }
        if (hasBoldStyle || hasItalicStyle) {
            mSpanSelectionListener?.onSelection(hasBoldStyle, hasItalicStyle)
        } else {
            mSpanSelectionListener?.onSelection(isBoldEnable = false, isItalicEnable = false)
        }
        recordSelStart = selStart
        recordSelEnd = selEnd
    }

    /**
     * 设置字符数限制
     */
    fun setFilterMaxLength(maxLength: Int) {
        mLengthFilterSize = maxLength
        updateFilters()
    }

    /**
     * 设置文本输入框的过滤器
     */
    private fun updateFilters() {
        val filterList: MutableList<InputFilter> = mutableListOf()
        filterList.add(mBoldAndItalicStyleFilter)
        mLengthFilterSize?.let { maxLength ->
            filterList.add(InputFilter.LengthFilter(maxLength))
        }
        filters = filterList.toTypedArray()
    }

    /**
     * 设置ubb
     */
    fun setUBB(ubb: String) {
        val ubbEditTextConvert = UBBEditTextConvert(this)
        ubbEditTextConvert.parseUBB(ubb)
    }

    /**
     * 获取ubb
     */
    fun getUBB(): String {
        return editableText?.getUbb() ?: ""
    }

    /**
     * 更新选中文本的样式
     */
    fun invalidateSelectionStyle() {
        val editableText = editableText ?: return
        if (editableText.isEmpty()) {
            return
        }
        val selStart = selectionStart
        val selEnd = selectionEnd
        /*
        包含自定义span不切换样式
         */
        val iSpans = editableText.getSpans(selStart, selEnd, ISpan::class.java)
        if (!iSpans.isNullOrEmpty()) {
            return
        }
        val isBoldEnable = mBoldEnable
        val isItalicEnable = mItalicEnable
        /*
        截断样式，移除目标文本部分的样式
         */
        editableText.getSpans(selStart, selEnd, StyleSpan::class.java)?.forEach { styleSpan ->
            val start = editableText.getSpanStart(styleSpan)
            val end = editableText.getSpanEnd(styleSpan)
            editableText.removeSpan(styleSpan)
            if (start in 0..selStart) {
                if (start != selStart) {
                    editableText.setSpan(
                        StyleSpan(styleSpan.style),
                        start,
                        selStart,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            if (end in selEnd..editableText.length) {
                if (selEnd != end) {
                    editableText.setSpan(
                        StyleSpan(styleSpan.style),
                        selEnd,
                        end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        /*
        为目标文本部分设置新样式
         */
        if (selStart == selEnd) {
            return
        }
        editableText.setSpan(
            StyleSpan(
                if (isBoldEnable && isItalicEnable) {
                    Typeface.BOLD_ITALIC
                } else if (isBoldEnable) {
                    Typeface.BOLD
                } else if (isItalicEnable) {
                    Typeface.ITALIC
                } else {
                    Typeface.NORMAL
                }
            ),
            selStart,
            selEnd,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * 插入span
     * @param span 文本
     * @param isInsertEnd 是否直接插在最后的内容
     */
    fun insertSpan(span: ISpan, isInsertEnd: Boolean = true) {
        var start: Int
        var end: Int
        if (isInsertEnd) {
            start = length()
            end = length()
        } else {
            start = selectionStart
            end = selectionEnd
            if (start < 0 || end < 0) {
                start = length()
                end = length()
            }
        }
        val ssb = SpannableStringBuilder()
        if (span is GlideImageSpan) {
            ssb.append(" ")
            ssb.setSpan(span, 0, 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            ssb.append(span.getSpanText())
            ssb.setSpan(span, 0, ssb.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        editableText.replace(start, end, ssb)
    }

    /**
     * 监听span的插入和移除
     */
    interface OnSpanChangedListener {
        fun onChanged(replacementSpanArray: Array<AbstractReplacementSpan>, isInsert: Boolean)
    }

    /**
     * 监听选中文字的span样式
     */
    interface OnSpanSelectionListener {
        fun onSelection(isBoldEnable: Boolean, isItalicEnable: Boolean)
    }

}