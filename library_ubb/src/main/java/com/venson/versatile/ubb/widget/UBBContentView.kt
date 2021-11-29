package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.bean.UBBViewType
import com.venson.versatile.ubb.convert.UBBContentViewConvert
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.utils.TypedArrayUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * UBB内容展示
 */
class UBBContentView : LinearLayout, DefaultLifecycleObserver {

    companion object {
        const val IMAGE_WIDTH_MATCH = -1
        const val IMAGE_WIDTH_WRAP = -2
    }

    @ColorInt
    private var mTextColor: Int = Color.BLACK

    @Px
    private var mTextSize: Float = 0F

    @Px
    private var mLineSpacingExtra: Float = 0F

    @Px
    private var mLineSpacingMultiplier: Float = 1.0F

    @Px
    private var mVerticalSpacing: Int = 0

    @Px
    private var mImageCorners: Float = 0F

    @Px
    private var mImageWidth: Int = IMAGE_WIDTH_MATCH

    @DrawableRes
    private var mImagePlaceholderRes: Int = 0

    private var mImagePlaceholderRatio: String = "2:1"

    private var mUBB: String? = null

    private var mAdapter: UBBContentAdapter? = null
    private val mUBBContentList = mutableListOf<UBBContentBean>()

    private val mUBBContentMap = mutableMapOf<Int, MutableList<String>>()

    private var mOnContentClickListener: OnContentClickListener? = null

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
        /*
        文本颜色
         */
        if (array.hasValue(R.styleable.UBBContentView_android_textColor)) {
            mTextColor = array.getColor(R.styleable.UBBContentView_android_textColor, Color.BLACK)
        }
        /*
        文本字号
         */
        mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            14F,
            resources.displayMetrics
        )
        if (array.hasValue(R.styleable.UBBContentView_android_textSize)) {
            mTextSize = array.getDimension(
                R.styleable.UBBContentView_android_textSize, mTextSize
            )
        }
        /*
        文本行间距
         */
        if (array.hasValue(R.styleable.UBBContentView_android_lineSpacingExtra)) {
            mLineSpacingExtra = array.getDimension(
                R.styleable.UBBContentView_android_lineSpacingExtra, mLineSpacingExtra
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_android_lineSpacingMultiplier)) {
            mLineSpacingMultiplier = array.getFloat(
                R.styleable.UBBContentView_android_lineSpacingMultiplier, mLineSpacingMultiplier
            )
        }
        /*
        媒体文件间隔
         */
        if (array.hasValue(R.styleable.UBBContentView_android_verticalSpacing)) {
            mVerticalSpacing = array.getDimensionPixelSize(
                R.styleable.UBBContentView_android_verticalSpacing, mVerticalSpacing
            )
        }
        /*
        图片展示宽度
         */
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
        /*
        图片圆角
         */
        if (array.hasValue(R.styleable.UBBContentView_imageCorners)) {
            mImageCorners = array.getDimension(
                R.styleable.UBBContentView_imageCorners, mImageCorners
            )
        }
        /*
        图片占位图
         */
        if (array.hasValue(R.styleable.UBBContentView_imagePlaceholder)) {
            mImagePlaceholderRes = array.getResourceId(
                R.styleable.UBBContentView_imagePlaceholder, 0
            )
        }
        val ratio = array.getString(R.styleable.UBBContentView_imagePlaceholderRatio)
        mImagePlaceholderRatio = if (ratio.isNullOrEmpty()) {
            "4:3"
        } else {
            if (!ratio.contains(":")) {
                throw Exception("UBBContentView_imagePlaceholderRatio 必须按格式填写 w:h")
            }
            ratio
        }
        initAdapterParams()
    }

    fun setAdapter(adapter: UBBContentAdapter) {
        mAdapter = adapter
        initAdapterParams()
    }

    private fun initAdapterParams() {
        val adapter = mAdapter ?: return
        adapter.initParams(
            mTextColor,
            mTextSize,
            mLineSpacingExtra,
            mLineSpacingMultiplier,
            mVerticalSpacing,
            mImageCorners,
            mImageWidth,
            mImagePlaceholderRes,
            mImagePlaceholderRatio
        )
    }

    fun setUBB(lifecycleOwner: LifecycleOwner, ubb: String?) {
        lifecycleOwner.lifecycle.let { lifecycle ->
            lifecycle.removeObserver(this)
            lifecycle.addObserver(this)
        }
        if (mUBB == ubb) {
            return
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            mUBBContentList.clear()
            mUBBContentMap.clear()
            removeAllViews()
            mAdapter?.let { ubbContentAdapter ->
                withContext(Dispatchers.IO) {
                    val ubbConvert = UBBContentViewConvert(context, ubbContentAdapter)
                    ubbConvert.parseUBB4SpannableStringBuilder(ubb)
                    val ubbContentBeanList = ubbConvert.getUBBContentBeanList()
                    fillContent(ubbContentAdapter, ubbContentBeanList)
                    mUBB = ubb
                }
            }
        }
    }

    /**
     * 填充视图
     */
    private suspend fun fillContent(
        adapter: UBBContentAdapter,
        ubbContentBeanList: List<UBBContentBean>
    ) {
        withContext(Dispatchers.IO) {
            ubbContentBeanList.forEachIndexed { position, ubbContentBean ->
                val type = ubbContentBean.type
                val contentList = mUBBContentMap[type] ?: let {
                    val list = mutableListOf<String>()
                    mUBBContentMap[type] = list
                    list
                }
                val data: String = if (type == UBBViewType.VIEW_IMAGE.type) {
                    ubbContentBean.style?.getAttr()?.get(ImageStyle.ATTR_SRC) ?: ""
                } else {
                    ""
                }
                contentList.add(data)
                withContext(Dispatchers.Main) {
                    if (type == UBBViewType.VIEW_TEXT.type || ubbContentBean.style == null) {
                        DefaultViewHolder.build(context)
                    } else {
                        adapter.onCreateViewHolder(
                            this@UBBContentView,
                            ubbContentBean.style!!
                        )
                    }?.let { holder ->
                        addView(holder.itemView)
                        adapter.onBindViewHolder(
                            this@UBBContentView,
                            holder,
                            position,
                            ubbContentBean
                        )
                        holder.itemView.setOnClickListener {
                            mOnContentClickListener?.onClick(
                                type,
                                data,
                                contentList,
                                contentList.indexOf(data)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mUBBContentList.clear()
        mUBBContentMap.clear()
        removeAllViews()
        mAdapter = null
    }

    fun setOnContentClickListener(listener: OnContentClickListener) {
        mOnContentClickListener = listener
    }

    abstract class ViewHolder(val itemView: View)

    interface OnContentClickListener {

        fun onClick(
            type: Int,
            data: String,
            dataList: MutableList<String>,
            position: Int
        )
    }
}