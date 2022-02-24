package com.venson.versatile.ubb.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.holder.AbcViewHolder
import com.venson.versatile.ubb.holder.DefaultViewHolder
import com.venson.versatile.ubb.widget.UBBContentView

/**
 * 内部使用的视图绑定器
 */
class UBBContentAdapter : RecyclerView.Adapter<AbcViewHolder>() {

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
    private var mImageLeftTopCorners: Float = 0F

    @Px
    private var mImageRightTopCorners: Float = 0F

    @Px
    private var mImageRightBottomCorners: Float = 0F

    @Px
    private var mImageLeftBottomCorners: Float = 0F

    private var mImageCornersEnableFlags: Int = 0

    @Px
    private var mImageWidth: Int = UBBContentView.IMAGE_WIDTH_MATCH

    @DrawableRes
    private var mImagePlaceholderRes: Int = 0

    private var mImagePlaceholderRatio: String = "2:1"

    private var mRecyclerView: RecyclerView? = null

    private var mOnItemClickListener: OnItemClickListener? = null

    private val mDataSizeMap: MutableMap<String, Point> = mutableMapOf()

    internal fun initParams(
        textColor: Int,
        textSize: Float,
        lineSpacingExtra: Float,
        lineSpacingMultiplier: Float,
        verticalSpacing: Int,
        horizontalSpacing: Int,
        imageLeftTopCorners: Float,
        imageRightTopCorners: Float,
        imageRightBottomCorners: Float,
        imageLeftBottomCorners: Float,
        imageCornersEnableFlags: Int,
        imageWidth: Int,
        imagePlaceholderRes: Int,
        imagePlaceholderRatio: String
    ) {
        mTextColor = textColor
        mTextSize = textSize
        mLineSpacingExtra = lineSpacingExtra
        mLineSpacingMultiplier = lineSpacingMultiplier
        mVerticalSpacing = verticalSpacing
        mHorizontalSpacing = horizontalSpacing
        mImageLeftTopCorners = imageLeftTopCorners
        mImageRightTopCorners = imageRightTopCorners
        mImageRightBottomCorners = imageRightBottomCorners
        mImageLeftBottomCorners = imageLeftBottomCorners
        mImageCornersEnableFlags = imageCornersEnableFlags
        mImageWidth = imageWidth
        mImagePlaceholderRes = imagePlaceholderRes
        mImagePlaceholderRatio = imagePlaceholderRatio
    }

    @ColorInt
    fun getTextColor(): Int = mTextColor

    @Px
    fun getTextSize(): Float = mTextSize

    @Px
    fun getLineSpacingExtra(): Float = mLineSpacingExtra

    @Px
    fun getLineSpacingMultiplier(): Float = mLineSpacingMultiplier

    @Px
    fun getVerticalSpacing(): Int = mVerticalSpacing

    @Px
    fun getHorizontalSpacing(): Int = mHorizontalSpacing

    @Px
    fun getImageCorners(): Array<Float> = arrayOf(
        mImageLeftTopCorners,
        mImageRightTopCorners,
        mImageRightBottomCorners,
        mImageLeftBottomCorners
    )

    fun getImageCornersEnableFlags(): Int = mImageCornersEnableFlags

    @Px
    fun getImageWidth(): Int = mImageWidth

    @Px
    fun getImagePlaceholderRes(): Int = mImagePlaceholderRes

    fun getImagePlaceholderRatio(): String = mImagePlaceholderRatio

    private var mContentData: List<UBBContentBean>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<UBBContentBean>?) {
        mContentData = data
        mDataSizeMap.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbcViewHolder {
        return UBB.getProvider(viewType)?.createViewHolder(parent.context)
            ?: DefaultViewHolder.build(parent.context)
    }

    override fun onBindViewHolder(holder: AbcViewHolder, position: Int) {
        val ubbContentBean = mContentData?.get(position) ?: return
        val parentWidth = mRecyclerView?.let { recyclerView ->
            if (recyclerView.measuredWidth > 0) {
                (recyclerView.measuredWidth
                        - recyclerView.paddingLeft
                        - recyclerView.paddingRight
                        - getHorizontalSpacing()
                        - getHorizontalSpacing())
            } else {
                0
            }
        } ?: 0
        holder.bindData(this, ubbContentBean, parentWidth)
        holder.updateMargin(
            holder.bindingAdapterPosition,
            getVerticalSpacing(),
            getHorizontalSpacing()
        )
        holder.setOnClickListener(object : OnItemClickListener {
            override fun onClick(type: Int, position: Int, view: View) {
                mOnItemClickListener?.onClick(type, position, view)
            }

        })
    }

    fun putSize(data: String, point: Point) {
        mDataSizeMap[data] = point
    }

    fun getSize(data: String): Point? {
        return mDataSizeMap[data]
    }

    override fun getItemViewType(position: Int): Int {
        val contentBean = mContentData?.get(position) ?: return 0
        return contentBean.type
    }

    override fun getItemCount(): Int {
        return mContentData?.size ?: 0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onClick(type: Int, position: Int, view: View)
    }
}