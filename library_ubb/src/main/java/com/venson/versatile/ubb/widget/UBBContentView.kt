package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.bean.ViewHolderType
import com.venson.versatile.ubb.convert.UBBContentViewConvert
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.utils.TypedArrayUtils
import kotlinx.coroutines.*
import kotlin.math.roundToInt

/**
 * UBB内容展示
 */
class UBBContentView : LinearLayout {

    companion object {
        private const val IMAGE_WIDTH_MATCH = -1
        private const val IMAGE_WIDTH_WRAP = -2
    }

    @ColorInt
    private var mTextColor: Int = Color.BLACK

    @Px
    private var mTextSize: Int? = null

    @Px
    private var mLineSpacingExtra: Int = 0
    private var mLineSpacingMultiplier: Float = 1.0F

    @Px
    private var mVerticalSpacing: Float = 0F

    @Px
    private var mImageCorners: Float = 0F

    private var mImageWidth: Int = IMAGE_WIDTH_MATCH

    private var mAdapter: UBBContentAdapter? = null
    private val mUBBContentList = mutableListOf<UBBContentBean>()

    private var mJob: Job? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttrs(context, attrs)
    }

    init {
        orientation = VERTICAL
        setAdapter(UBB.getContentAdapter())
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        attrs ?: return
        val array = TypedArrayUtils.obtainAttributes(
            resources,
            context.theme,
            attrs,
            R.styleable.UBBContentView
        )
        if (array.hasValue(R.styleable.UBBContentView_android_textColor)) {
            mTextColor = array.getColor(R.styleable.UBBContentView_android_textColor, Color.BLACK)
        }
        if (array.hasValue(R.styleable.UBBContentView_android_textSize)) {
            mTextSize = array.getDimensionPixelSize(
                R.styleable.UBBContentView_android_textSize, 0
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_android_lineSpacingExtra)) {
            mLineSpacingExtra = array.getDimensionPixelSize(
                R.styleable.UBBContentView_android_lineSpacingExtra, mLineSpacingExtra
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_android_lineSpacingMultiplier)) {
            mLineSpacingMultiplier = array.getFloat(
                R.styleable.UBBContentView_android_lineSpacingMultiplier, mLineSpacingMultiplier
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_android_verticalSpacing)) {
            mVerticalSpacing = array.getDimension(
                R.styleable.UBBContentView_android_verticalSpacing, mVerticalSpacing
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageWidth)) {
            val value = TypedValue()
            array.getValue(R.styleable.UBBContentView_imageWidth, value)
            mImageWidth = if (value.data == IMAGE_WIDTH_MATCH || value.data == IMAGE_WIDTH_WRAP) {
                value.data
            } else if (value.type == TypedValue.TYPE_DIMENSION) {
                value.getDimension(context.resources.displayMetrics).roundToInt()
            } else {
                IMAGE_WIDTH_MATCH
            }
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCorners)) {
            mImageCorners = array.getDimension(
                R.styleable.UBBContentView_imageCorners, mImageCorners
            )
        }
    }

    fun setAdapter(adapter: UBBContentAdapter) {
        mAdapter = adapter
    }

    fun setUBB(ubb: String?) {
        mUBBContentList.clear()
        removeAllViews()
        mJob?.cancel()
        mJob = GlobalScope.async(Dispatchers.IO) {
            mAdapter?.let { adapter ->
                val ubbConvert = UBBContentViewConvert(context, adapter)
                ubbConvert.parseUBB4SpannableStringBuilder(ubb)
                val ubbContentBeanList = ubbConvert.getUBBContentBeanList()
                fillContent(adapter, ubbContentBeanList)
            }
        }
    }

    /**
     * 填充视图
     */
    suspend fun fillContent(adapter: UBBContentAdapter, ubbContentBeanList: List<UBBContentBean>) {
        withContext(Dispatchers.IO) {
            ubbContentBeanList.forEachIndexed { position, ubbContentBean ->
                withContext(Dispatchers.Main) {
                    if (ubbContentBean.type == ViewHolderType.VIEW_TEXT.type
                        || ubbContentBean.style == null
                    ) {
                        DefaultViewHolder.build(context)
                    } else {
                        adapter.onCreateViewHolder(
                            this@UBBContentView,
                            ubbContentBean.style!!
                        )
                    }?.let { holder ->
                        addView(holder.itemView)
                        adapter.onBindViewHolder(holder, position, ubbContentBean)
                    }
                }
            }
        }
    }

//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        mJob?.cancel()
//        mJob?.start()
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mJob?.cancel()
    }

    open class ViewHolder(val itemView: View)
}