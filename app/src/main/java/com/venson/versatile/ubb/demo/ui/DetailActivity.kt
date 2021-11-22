package com.venson.versatile.ubb.demo.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.venson.versatile.ubb.adapter.UBBContentAdapter
import com.venson.versatile.ubb.demo.DataBean
import com.venson.versatile.ubb.demo.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailBinding

    companion object {

        fun startActivity(context: Context, dataBean: DataBean) {
            context.startActivity(
                Intent(context, DetailActivity::class.java).also { intent ->
                    intent.putExtra("data", dataBean)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val dataBean = intent?.getParcelableExtra<DataBean>("data")
        mBinding.titleView.text = dataBean?.title ?: ""
        mBinding.contentView.setUBB(dataBean?.content)
    }
}