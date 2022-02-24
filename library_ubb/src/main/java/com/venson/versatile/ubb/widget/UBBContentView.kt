package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.adapter.HeadOrFootAdapter
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.convert.UBBContentViewConvert
import com.venson.versatile.ubb.holder.ImageViewHolder
import com.venson.versatile.ubb.style.ImageStyle
import com.venson.versatile.ubb.utils.TypedArrayUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


/**
 * UBB内容展示
 */
class UBBContentView : RecyclerView, DefaultLifecycleObserver {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
    }

    companion object {
        const val IMAGE_WIDTH_MATCH = -1
        const val IMAGE_WIDTH_WRAP = -2

        const val IMAGE_CORNERS_ENABLE_ANY_FLAG = 0x10
        const val IMAGE_CORNERS_ENABLE_FIRST_FLAG = 0x20
        const val IMAGE_CORNERS_ENABLE_LAST_FLAG = 0x02
        const val IMAGE_CORNERS_ENABLE_EDGE_FLAG = 0x22
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
    private var mHorizontalSpacing: Int = 0

    @Px
    private var mHeadSpacing: Int = 0

    @Px
    private var mFootSpacing: Int = 0

    @Px
    private var mImageLeftTopCorners: Float = 0F

    @Px
    private var mImageLeftBottomCorners: Float = 0F

    @Px
    private var mImageRightTopCorners: Float = 0F

    @Px
    private var mImageRightBottomCorners: Float = 0F

    //图片圆角应用方式
    private var mImageCornersEnableFlag: Int = IMAGE_CORNERS_ENABLE_ANY_FLAG

    @Px
    private var mImageWidth: Int = IMAGE_WIDTH_MATCH

    @DrawableRes
    private var mImagePlaceholderRes: Int = 0

    private var mImagePlaceholderRatio: String = "2:1"

    private var mUBB: String? = null

    private var mAdapter: UBBContentAdapter = UBBContentAdapter()
    private val mUBBContentList = mutableListOf<UBBContentBean>()

    private val mUBBChildDataMap = mutableMapOf<Int, MutableList<String>>()
    private val mUBBChildIndexMap = mutableMapOf<Int, MutableList<Int>>()

    private var mOnImageClickListener: OnImageClickListener? = null

    private var mHeadView: View? = null
    private var mFootView: View? = null

    private val mHeaderAdapterList: MutableList<HeadOrFootAdapter> = mutableListOf()
    private val mFooterAdapterList: MutableList<HeadOrFootAdapter> = mutableListOf()

    private val linearLayout by lazy { LinearLayout(context) }

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        if (isInEditMode) {
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = LayoutParams(500, 500)
        }
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
        内容横向间距
         */
        if (array.hasValue(R.styleable.UBBContentView_android_horizontalSpacing)) {
            mHorizontalSpacing = array.getDimensionPixelSize(
                R.styleable.UBBContentView_android_horizontalSpacing, mHorizontalSpacing
            )
        }
        /*
        内容与head的距离
         */
        if (array.hasValue(R.styleable.UBBContentView_headSpacing)) {
            mHeadSpacing = array.getDimensionPixelSize(
                R.styleable.UBBContentView_headSpacing, mHeadSpacing
            )
        }
        /*
        内容与food的距离
         */
        if (array.hasValue(R.styleable.UBBContentView_footSpacing)) {
            mFootSpacing = array.getDimensionPixelSize(
                R.styleable.UBBContentView_footSpacing, mFootSpacing
            )
        }
        /*
        header布局id
         */
        if (array.hasValue(R.styleable.UBBContentView_header)) {
            val layoutResId = array.getResourceId(
                R.styleable.UBBContentView_header, 0
            )
            if (layoutResId != 0) {
                try {
                    mHeadView = LayoutInflater.from(context).inflate(
                        layoutResId,
                        if (isInEditMode) {
                            linearLayout
                        } else {
                            this
                        },
                        false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        /*
        footer布局id
         */
        if (array.hasValue(R.styleable.UBBContentView_footer)) {
            val layoutResId = array.getResourceId(
                R.styleable.UBBContentView_footer, 0
            )
            if (layoutResId != 0) {
                try {
                    mFootView = LayoutInflater.from(context).inflate(
                        layoutResId,
                        if (isInEditMode) {
                            linearLayout
                        } else {
                            this
                        },
                        false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
        图片圆角应用规则
         */
        if (array.hasValue(R.styleable.UBBContentView_imageCornersEnableFlags)) {
            mImageCornersEnableFlag = array.getInt(
                R.styleable.UBBContentView_imageCornersEnableFlags,
                IMAGE_CORNERS_ENABLE_ANY_FLAG
            )
        }
        /*
        图片圆角
         */
        if (array.hasValue(R.styleable.UBBContentView_imageCorners)) {
            val defaultCorners = array.getDimension(
                R.styleable.UBBContentView_imageCorners, 0F
            )
            mImageLeftTopCorners = defaultCorners
            mImageRightTopCorners = defaultCorners
            mImageRightBottomCorners = defaultCorners
            mImageLeftBottomCorners = defaultCorners
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersLeft)) {
            mImageLeftTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftTop, mImageLeftTopCorners
            )
            mImageLeftBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftBottom, mImageLeftBottomCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersTop)) {
            mImageLeftTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftTop, mImageLeftTopCorners
            )
            mImageRightTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightTop, mImageRightTopCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersRight)) {
            mImageRightTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightTop, mImageRightTopCorners
            )
            mImageRightBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightBottom, mImageRightBottomCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersBottom)) {
            mImageLeftBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftBottom, mImageLeftBottomCorners
            )
            mImageRightBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightBottom, mImageRightBottomCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersLeftTop)) {
            mImageLeftTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftTop, mImageLeftTopCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersRightTop)) {
            mImageRightTopCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightTop, mImageRightTopCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersRightBottom)) {
            mImageRightBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersRightBottom, mImageRightBottomCorners
            )
        }
        if (array.hasValue(R.styleable.UBBContentView_imageCornersLeftBottom)) {
            mImageLeftBottomCorners = array.getDimension(
                R.styleable.UBBContentView_imageCornersLeftBottom, mImageLeftBottomCorners
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

    /**
     * 初始化adapter参数
     */
    private fun initAdapterParams() {
        mAdapter.initParams(
            mTextColor,
            mTextSize,
            mLineSpacingExtra,
            mLineSpacingMultiplier,
            mVerticalSpacing,
            mHorizontalSpacing,
            mImageLeftTopCorners,
            mImageRightTopCorners,
            mImageRightBottomCorners,
            mImageLeftBottomCorners,
            mImageCornersEnableFlag,
            mImageWidth,
            mImagePlaceholderRes,
            mImagePlaceholderRatio
        )
        mAdapter.setOnItemClickListener(
            object : UBBContentAdapter.OnItemClickListener {
                override fun onClick(type: Int, position: Int, view: View) {
                    val provider = UBB.getProvider(type) ?: return
                    val dataList = mUBBChildDataMap[type] ?: return
                    val indexList = mUBBChildIndexMap[type] ?: return
                    if (provider.getUBBTagName().equals(ImageStyle.TAG_NAME, true)) {
                        (view as? ImageView)?.let {
                            var indexOfData = 0
                            for (index in 0 until indexList.size) {
                                if (indexList[index] == position) {
                                    indexOfData = index
                                    break
                                }
                            }
                            mOnImageClickListener?.onClick(dataList, indexOfData, it)
                        }
                    }
                }

            }
        )
        mHeadView?.let {
            if (isInEditMode) {
                linearLayout.addView(it)
                it.layoutParams?.let { layoutParams ->
                    layoutParams as MarginLayoutParams
                    layoutParams.bottomMargin = mHeadSpacing
                }
            } else {
                addHeader(it)
            }
        }
        if (isInEditMode) {
            addTextView(
                0,
                SpannableStringBuilder()
                    .append("Android Studio is Android's official IDE. ")
                    .append("It is purpose-built for Android to accelerate your development and ")
                    .append("help you build the highest-quality apps for every Android device.")
            )
            addImageView(0, 3)
            addImageView(1, 3)
            addImageView(2, 3)
            addTextView(
                1,
                SpannableStringBuilder()
                    .append("Based on Intellij IDEA, ")
                    .append("Android Studio provides the fastest possible ")
                    .append("turnaround on your coding and running workflow.")
            )
        }
        mFootView?.let {
            if (isInEditMode) {
                linearLayout.addView(it)
                it.layoutParams?.let { layoutParams ->
                    layoutParams as MarginLayoutParams
                    layoutParams.topMargin = mFootSpacing
                }
            } else {
                addFooter(it)
            }
        }
        if (mHeaderAdapterList.isEmpty() && mFooterAdapterList.isEmpty()) {
            updateAdapter()
        }
    }

    /**
     * 设置ubb文本
     */
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
            mUBBChildDataMap.clear()
            mUBBChildIndexMap.clear()
            removeAllViews()
            withContext(Dispatchers.IO) {
                val ubbConvert = UBBContentViewConvert(context)
                ubbConvert.parseUBB4SpannableStringBuilder(ubb)
                val ubbContentBeanList = ubbConvert.getUBBContentBeanList()
                ubbContentBeanList.forEachIndexed { index, ubbContentBean ->
                    val type = ubbContentBean.type
                    val childDataList = mUBBChildDataMap[type] ?: let {
                        val list = mutableListOf<String>()
                        mUBBChildDataMap[type] = list
                        list
                    }
                    val childIndexList = mUBBChildIndexMap[type] ?: let {
                        val list = mutableListOf<Int>()
                        mUBBChildIndexMap[type] = list
                        list
                    }
                    if (UBB.getProvider(type)?.equalsTag(ImageStyle.TAG_NAME) == true) {
                        val data: String = (ubbContentBean.style as? ImageStyle)
                            ?.getRealPath(context) ?: ""
                        childDataList.add(data)
                        childIndexList.add(index)
                    }
                }
                withContext(Dispatchers.Main) {
                    mAdapter.setData(ubbContentBeanList)
                    mUBB = ubb
                }
            }
        }
    }

    /**
     * 滚动到指定view位置
     */
    fun scrollToIndex(index: Int, listener: OnImageScrollDisplayListener) {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return
        try {
            val childIndex = getChildIndex(ImageStyle.TAG_NAME, index)
            layoutManager.scrollToPositionWithOffset(childIndex, 0)
            postDelayed(
                {
                    val firstPosition = layoutManager.findFirstVisibleItemPosition()
                    val childView = layoutManager.getChildAt(childIndex - firstPosition)
                    (childView?.let { getChildViewHolder(it) } as? ImageViewHolder)?.let { holder ->
                        listener.onScrollDisplay(holder.itemView, holder.imageView)
                    }
                },
                100L
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getChildIndex(tagName: String, index: Int): Int {
        UBB.getProvider(tagName)?.let { provider ->
            val type = UBB.getViewType(provider)
            mUBBChildIndexMap[type]?.get(index)?.let { childIndex ->
                var headerCount = 0
                mHeaderAdapterList.forEach { headerAdapter ->
                    headerCount += headerAdapter.itemCount
                }
                return headerCount + childIndex
            }
        }
        return 0
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mUBBContentList.clear()
        mUBBChildDataMap.clear()
        removeAllViews()
    }

    fun setOnImageClickListener(listener: OnImageClickListener) {
        mOnImageClickListener = listener
    }

    /**
     * 添加Header
     */
    fun addHeader(view: View) {
        val adapter = HeadOrFootAdapter(view)
        adapter.updateSpacing(mHeadSpacing, 0)
        mHeaderAdapterList.add(adapter)
        updateAdapter()
    }

    /**
     * 移除Header
     */
    fun removeHeader(view: View) {
        mHeaderAdapterList.iterator().let { iterator ->
            while (iterator.hasNext()) {
                val adapter = iterator.next()
                if (adapter.view == view) {
                    iterator.remove()
                    updateAdapter()
                    break
                }
            }
        }
    }

    /**
     * 清空Header
     */
    fun clearHeader() {
        if (mHeaderAdapterList.isEmpty()) {
            return
        }
        mHeaderAdapterList.clear()
        updateAdapter()
    }

    fun getHeaderView(): View? {
        return mHeadView
    }

    /**
     * 添加Footer
     */
    fun addFooter(view: View) {
        val adapter = HeadOrFootAdapter(view)
        adapter.updateSpacing(0, mFootSpacing)
        mFooterAdapterList.add(adapter)
        updateAdapter()
    }

    /**
     * 移除Footer
     */
    fun removeFooter(view: View) {
        mFooterAdapterList.iterator().let { iterator ->
            while (iterator.hasNext()) {
                val adapter = iterator.next()
                if (adapter.view == view) {
                    iterator.remove()
                    updateAdapter()
                    break
                }
            }
        }
    }

    /**
     * 清空Footer
     */
    fun clearFooter() {
        if (mFooterAdapterList.isEmpty()) {
            return
        }
        mFooterAdapterList.clear()
        updateAdapter()
    }

    fun getFooterView(): View? {
        return mFootView
    }

    /**
     * 组合adapter
     */
    private fun updateAdapter() {
        val adapterList = mutableListOf<Adapter<out ViewHolder>>()
        mHeaderAdapterList.forEach {
            adapterList.add(it)
        }
        adapterList.add(mAdapter)
        mFooterAdapterList.forEach {
            adapterList.add(it)
        }
        val concatAdapter = ConcatAdapter(adapterList)
        adapter = concatAdapter
    }

    interface OnImageClickListener {
        fun onClick(pathList: List<String>, index: Int, view: ImageView)
    }

    interface OnImageScrollDisplayListener {
        fun onScrollDisplay(itemView: View, imageView: ImageView)
    }

    /**
     * 添加预览文本
     */
    private fun addTextView(index: Int, text: SpannableStringBuilder) {
        if (!isInEditMode) {
            return
        }
        val textView = TextView(context).also {
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
            it.setTextColor(mTextColor)
            it.setLineSpacing(mLineSpacingExtra, mLineSpacingMultiplier)
            text.setSpan(
                ForegroundColorSpan(mTextColor),
                0,
                text.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            it.text = text
        }
        linearLayout.addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        textView.layoutParams?.let { layoutParams ->
            layoutParams as MarginLayoutParams
            layoutParams.leftMargin = mHorizontalSpacing
            layoutParams.rightMargin = mHorizontalSpacing
            layoutParams.topMargin = if (index == 0) {
                0
            } else {
                mVerticalSpacing
            }
        }
    }

    /**
     * 添加图片预览样式
     */
    private fun addImageView(index: Int, count: Int) {
        if (!isInEditMode) {
            return
        }
        val constraintLayout = ConstraintLayout(context)
        val imageView = ShapeableImageView(context)
        if (mImagePlaceholderRes == 0) {
            imageView.setImageDrawable(ColorDrawable(Color.LTGRAY))
        } else {
            imageView.setImageResource(mImagePlaceholderRes)
        }
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.shapeAppearanceModel = ShapeAppearanceModel.builder().apply {
            if (mImageCornersEnableFlag.or(IMAGE_CORNERS_ENABLE_ANY_FLAG)
                == mImageCornersEnableFlag
            ) {
                setTopLeftCorner(CornerFamily.ROUNDED, mImageLeftTopCorners)
                setTopRightCorner(CornerFamily.ROUNDED, mImageRightTopCorners)
                setBottomRightCorner(CornerFamily.ROUNDED, mImageRightBottomCorners)
                setBottomLeftCorner(CornerFamily.ROUNDED, mImageLeftBottomCorners)
                return@apply
            }
            if (index == 0) {
                if (mImageCornersEnableFlag.or(IMAGE_CORNERS_ENABLE_FIRST_FLAG)
                    == mImageCornersEnableFlag
                    || mImageCornersEnableFlag.or(IMAGE_CORNERS_ENABLE_EDGE_FLAG)
                    == mImageCornersEnableFlag
                ) {
                    setTopLeftCorner(CornerFamily.ROUNDED, mImageLeftTopCorners)
                    setTopRightCorner(CornerFamily.ROUNDED, mImageRightTopCorners)
                }
            }
            if (index == count - 1) {
                if (mImageCornersEnableFlag.or(IMAGE_CORNERS_ENABLE_LAST_FLAG)
                    == mImageCornersEnableFlag
                    || mImageCornersEnableFlag.or(IMAGE_CORNERS_ENABLE_EDGE_FLAG)
                    == mImageCornersEnableFlag
                ) {
                    setBottomRightCorner(CornerFamily.ROUNDED, mImageRightBottomCorners)
                    setBottomLeftCorner(CornerFamily.ROUNDED, mImageLeftBottomCorners)
                }
            }
        }.build()
        constraintLayout.addView(
            imageView,
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, 0
            ).also {
                it.dimensionRatio = mImagePlaceholderRatio
                it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
        linearLayout.addView(constraintLayout, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        constraintLayout.layoutParams?.let { layoutParams ->
            layoutParams as MarginLayoutParams
            layoutParams.leftMargin = mHorizontalSpacing
            layoutParams.rightMargin = mHorizontalSpacing
            layoutParams.topMargin = mVerticalSpacing
        }
    }

    override fun draw(c: Canvas?) {
        super.draw(c)
        if (isInEditMode) {
            /*
            去除默认数据
             */
            c?.drawRect(
                Rect(0, 0, width, height),
                Paint().also { paint ->
                    paint.color = Color.WHITE
                    background?.let { background ->
                        if (background is ColorDrawable) {
                            paint.color = background.color
                        }
                    }
                    paint.style = Paint.Style.FILL
                }
            )
            /*
            计算高度
             */
            val measuredWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val measuredHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            var layoutHeight = 0
            for (index in 0 until linearLayout.childCount) {
                val child = linearLayout.getChildAt(index)
                child.measure(measuredWidth, measuredHeight)
                layoutHeight += child.measuredHeight
            }
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY);
            /*
            预览视图绘制
             */
            linearLayout.measure(measuredWidth, heightMeasureSpec)
            linearLayout.layout(0, 0, linearLayout.measuredWidth, linearLayout.measuredHeight)
            linearLayout.draw(c)
        }
    }
}