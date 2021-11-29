package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.venson.versatile.ubb.demo.databinding.ActivityDetailBinding
import com.venson.versatile.ubb.style.ImageStyle
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
        mBinding.titleView.text = dataBean?.title ?: ""
        mBinding.contentView.setUBB(this, dataBean?.content)
        mBinding.contentView.setOnContentClickListener(object :
            UBBContentView.OnContentClickListener {
            override fun onClick(
                type: Int,
                data: String,
                dataList: MutableList<String>,
                position: Int
            ) {
                if (type == ImageStyle.Helper.getViewType()) {
                    Toast.makeText(
                        this@DetailByContentActivity,
                        "position=$position;data=$data",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }
}