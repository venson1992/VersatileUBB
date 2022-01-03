package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.SmartGlideImageLoader
import com.venson.versatile.ubb.demo.databinding.ActivityDetailBinding
import com.venson.versatile.ubb.demo.databinding.ActivityDetailFooterBinding
import com.venson.versatile.ubb.demo.databinding.ActivityDetailHeaderBinding
import com.venson.versatile.ubb.widget.UBBContentView

class DetailByContentActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailBinding

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
        mBinding.contentView.addHeader(HeaderAdapter(dataBean))
        mBinding.contentView.addFooter(FooterAdapter())
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

    class HeaderAdapter(private val dataBean: DataBean?) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val headerBinding = ActivityDetailHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return object : RecyclerView.ViewHolder(headerBinding.root) {

            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val headerBinding = ActivityDetailHeaderBinding.bind(holder.itemView)
            headerBinding.titleView.text = dataBean?.title ?: ""
        }

        override fun getItemCount(): Int = 1

    }

    class FooterAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val footerBinding = ActivityDetailFooterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return object : RecyclerView.ViewHolder(footerBinding.root) {

            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

        override fun getItemCount(): Int = 1

    }
}