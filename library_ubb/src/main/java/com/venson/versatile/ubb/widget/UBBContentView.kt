package com.venson.versatile.ubb.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.convert.UBBContentViewConvert
import kotlinx.coroutines.*

/**
 * UBB内容展示
 */
class UBBContentView : LinearLayout {

    private var mAdapter: UBBContentAdapter? = null
    private val mUBBContentList = mutableListOf<UBBContentBean>()

    private var mJob: Job? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        orientation = VERTICAL
        setAdapter(UBB.getContentAdapter())
    }

    fun setAdapter(adapter: UBBContentAdapter) {
        mAdapter = adapter
    }

    fun setUBB(ubb: String?) {
        mUBBContentList.clear()
        removeAllViews()
        mJob?.cancel()
        mJob = GlobalScope.async(Dispatchers.IO) {
            val ubbConvert = UBBContentViewConvert()
            ubbConvert.parseUBB(ubb)
            val ubbContentBeanList = ubbConvert.getUBBContentBeanList()
            mAdapter?.let { adapter ->
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
                    val holder = adapter.onCreateViewHolder(
                        this@UBBContentView, ubbContentBean.type
                    )
                    addView(holder.itemView)
                    adapter.onBindViewHolder(holder, position, ubbContentBean)
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