package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.venson.versatile.ubb.demo.databinding.ActivityDetailBinding
import com.venson.versatile.ubb.demo.databinding.ActivityDetailFooterBinding
import com.venson.versatile.ubb.demo.databinding.ActivityDetailHeaderBinding
import com.venson.versatile.ubb.widget.UBBContentView

class DetailByContentActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailBinding

    private lateinit var mHeaderBinding: ActivityDetailHeaderBinding
    private lateinit var mFooterBinding: ActivityDetailFooterBinding

    companion object {

        fun startActivity(context: Context, dataBean: DataBean) {
            context.startActivity(
                Intent(context, DetailByContentActivity::class.java).also { intent ->
                    intent.putExtra("data", dataBean)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = UBBContentView::class.java.simpleName
        val dataBean = intent?.getParcelableExtra<DataBean>("data")
        /*
        header
         */
        mHeaderBinding = ActivityDetailHeaderBinding
            .inflate(layoutInflater, mBinding.contentView, false)
        mHeaderBinding.titleView.text = dataBean?.title ?: ""
        mBinding.contentView.addHeader(mHeaderBinding.root)
        /*
        footer
         */
        mFooterBinding = ActivityDetailFooterBinding
            .inflate(layoutInflater, mBinding.contentView, false)
        mBinding.contentView.addFooter(mFooterBinding.root)
        /*
        ubb相关
         */
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