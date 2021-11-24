package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.venson.versatile.ubb.convert.UBBTextViewConvert
import com.venson.versatile.ubb.demo.databinding.ActivityDetailByTextBinding
import com.venson.versatile.ubb.widget.UBBTextView

class DetailByTextActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailByTextBinding

    companion object {

        fun startActivity(context: Context, dataBean: DataBean) {
            context.startActivity(
                Intent(context, DetailByTextActivity::class.java).also { intent ->
                    intent.putExtra("data", dataBean)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailByTextBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = UBBTextView::class.java.simpleName
        val dataBean = intent?.getParcelableExtra<DataBean>("data")
        mBinding.titleView.text = dataBean?.title ?: ""
        val ubbTextViewConvert = UBBTextViewConvert(mBinding.contentView)
        ubbTextViewConvert.parseUBB(dataBean?.content)
        mBinding.contentView.setTextIsSelectable(true)
    }
}