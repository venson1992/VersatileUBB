package com.venson.versatile.ubb.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.bean.UBBContentBean
import com.venson.versatile.ubb.convert.UBBContentViewConvert
import kotlinx.coroutines.*

/**
 * UBB内容展示
 */
class UBBContentView : RecyclerView {

    private val mUBBContentList = mutableListOf<UBBContentBean>()

    private var mJob: Job? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
    }

    fun setUBB(ubb: String?) {
        mUBBContentList.clear()
        adapter = null
        mJob = GlobalScope.async(Dispatchers.IO) {
            val ubbConvert = UBBContentViewConvert()
            ubbConvert.parseUBB(ubb)
            val ubbContentBeanList = ubbConvert.getUBBContentBeanList()
            withContext(Dispatchers.Main) {
                adapter = UBBContentAdapter(ubbContentBeanList)
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

}