package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.venson.versatile.ubb.demo.databinding.ActivityDetailByContent2Binding
import com.venson.versatile.ubb.widget.UBBContentView

class DetailByContent2Activity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailByContent2Binding

    companion object {

        fun startActivity(context: Context, dataBean: DataBean) {
            context.startActivity(
                Intent(context, DetailByContent2Activity::class.java).also { intent ->
                    intent.putExtra("data", dataBean)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailByContent2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = UBBContentView::class.java.simpleName
        val dataBean = intent?.getParcelableExtra<DataBean>("data")
        mBinding.titleView.text = dataBean?.title ?: ""
        mBinding.contentView.setUBB(this, dataBean?.content)
        mBinding.contentView.setOnImageClickListener(object : UBBContentView.OnImageClickListener {
            override fun onClick(pathList: List<String>, index: Int, view: ImageView) {
                XPopup.Builder(view.context)
                    .asImageViewer(
                        view,
                        index,
                        pathList,
                        { popupView, position ->
                            mBinding.contentView.scrollToIndex(
                                position,
                                object : UBBContentView.OnImageScrollDisplayListener {
                                    override fun onScrollDisplay(
                                        itemView: View,
                                        imageView: ImageView
                                    ) {
                                        mBinding.scrollView.scrollTo(
                                            0,
                                            mBinding.contentView.top + itemView.top
                                        )
                                        popupView.updateSrcView(imageView)
                                    }

                                }
                            )
                        },
                        SmartGlideImageLoader()
                    )
                    .show()
            }

        })
    }

}