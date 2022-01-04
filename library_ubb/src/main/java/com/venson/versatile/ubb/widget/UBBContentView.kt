package com.venson.versatile.ubb.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
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
    private var mImageCorners: Float = 0F

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

    private val mHeaderAdapterList: MutableList<HeadOrFootAdapter<out ViewBinding>> =
        mutableListOf()
    private val mFooterAdapterList: MutableList<HeadOrFootAdapter<out ViewBinding>> =
        mutableListOf()

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        setAdapter(mAdapter)
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
        this.adapter = adapter
        initAdapterParams()
    }

    private fun initAdapterParams() {
        mAdapter.initParams(
            mTextColor,
            mTextSize,
            mLineSpacingExtra,
            mLineSpacingMultiplier,
            mVerticalSpacing,
            mHorizontalSpacing,
            mImageCorners,
            mImageWidth,
            mImagePlaceholderRes,
            mImagePlaceholderRatio
        )
        updateAdapter()
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
    fun addHeader(adapter: HeadOrFootAdapter<out ViewBinding>) {
        if (mHeaderAdapterList.contains(adapter)) {
            return
        }
        mHeaderAdapterList.add(adapter)
        updateAdapter()
    }

    /**
     * 移除Header
     */
    fun removeHeader(adapter: HeadOrFootAdapter<out ViewBinding>) {
        if (mHeaderAdapterList.contains(adapter)) {
            mHeaderAdapterList.remove(adapter)
        }
        updateAdapter()
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

    /**
     * 添加Footer
     */
    fun addFooter(adapter: HeadOrFootAdapter<out ViewBinding>) {
        if (mFooterAdapterList.contains(adapter)) {
            return
        }
        mFooterAdapterList.add(adapter)
        updateAdapter()
    }

    /**
     * 移除Footer
     */
    fun removeFooter(adapter: HeadOrFootAdapter<out ViewBinding>) {
        if (mFooterAdapterList.contains(adapter)) {
            mFooterAdapterList.remove(adapter)
        }
        updateAdapter()
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
}