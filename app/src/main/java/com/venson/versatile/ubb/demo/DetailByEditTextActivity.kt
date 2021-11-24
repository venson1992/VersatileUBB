package com.venson.versatile.ubb.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.venson.versatile.ubb.demo.databinding.ActivityDetailByEditTextBinding
import com.venson.versatile.ubb.widget.UBBEditText

class DetailByEditTextActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDetailByEditTextBinding

    companion object {

        fun startActivity(context: Context, dataBean: DataBean) {
            context.startActivity(
                Intent(context, DetailByEditTextActivity::class.java).also { intent ->
                    intent.putExtra("data", dataBean)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailByEditTextBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = UBBEditText::class.java.simpleName
        val dataBean = intent?.getParcelableExtra<DataBean>("data")
        mBinding.editText.setUBB(dataBean?.content ?: "")
    }
}