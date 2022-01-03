package com.venson.versatile.ubb.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEachIndexed
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.venson.versatile.ubb.ImageEngine
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.convert.UBBSimpleConvert
import com.venson.versatile.ubb.demo.databinding.ActivityMainBinding
import com.venson.versatile.ubb.demo.databinding.ItemLayoutContentBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var listViewModel: ListViewModel

    private var mMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listViewModel = ViewModelProvider(this).get(ListViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val root = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(
            root.context, RecyclerView.VERTICAL, false
        )
        UBB.setImageEngine(object : ImageEngine() {

            override fun getDomain(): String {
                return "file.25game.com"
            }

            override fun getSchema(): String {
                return "http"
            }

        })
        val adapter = ContentAdapter()
        binding.recyclerView.adapter = adapter
        listViewModel.mDataList.observe(this) {
            if (it != null) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            adapter.setData(it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            listViewModel.readTestData(root.context)
        }
        listViewModel.readTestData(root.context)
        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener {
            navView.menu.forEachIndexed { index, item ->
                if (it.itemId == item.itemId) {
                    mMode = index + 1
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    inner class ContentAdapter : RecyclerView.Adapter<ContentViewHolder>() {

        private var mList: List<DataBean>? = null

        @SuppressLint("NotifyDataSetChanged")
        fun setData(list: List<DataBean>?) {
            mList = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
            val itemBinding = ItemLayoutContentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ContentViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
            val binding: ItemLayoutContentBinding = holder.binding as? ItemLayoutContentBinding
                ?: return
            val dataBean = try {
                mList?.get(position)
            } catch (e: Exception) {
                null
            }
            binding.titleView.text = dataBean?.title ?: ""
            val ubb = dataBean?.content ?: ""
            val spannableStringUBBConvert = UBBSimpleConvert(this@MainActivity)
            spannableStringUBBConvert.parseUBB(ubb)
            val content = spannableStringUBBConvert.getContent()
            if (content.isEmpty()) {
                binding.contentView.visibility = View.GONE
            } else {
                binding.contentView.visibility = View.VISIBLE
                binding.contentView.text = content
                UBB.log("onBindViewHolder", "content->$content")
            }
            binding.root.setOnClickListener {
                if (dataBean == null) {
                    Toast.makeText(it.context, "数据为空", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                when (mMode) {
                    1 -> {
                        DetailByContentActivity.startActivity(this@MainActivity, dataBean)
//                        DetailByContent2Activity.startActivity(this@MainActivity, dataBean)
                    }
                    2 -> {
                        DetailByTextActivity.startActivity(this@MainActivity, dataBean)
                    }
                    else -> {
                        DetailByEditTextActivity.startActivity(this@MainActivity, dataBean)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

    }

    inner class ContentViewHolder(itemBinding: ViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        val binding = itemBinding

    }
}