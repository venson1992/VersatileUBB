package com.venson.versatile.ubb.demo.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.venson.versatile.ubb.demo.databinding.FragmentHomeBinding
import com.venson.versatile.ubb.demo.databinding.ItemLayoutContentBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(
            root.context, RecyclerView.VERTICAL, false
        )
        val adapter = ContentAdapter()
        binding.recyclerView.adapter = adapter
        homeViewModel.mDataList.observe(requireActivity()) {
            if (it != null) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            adapter.setData(it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.readTestData(root.context)
        }
        homeViewModel.readTestData(root.context)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ContentAdapter : RecyclerView.Adapter<ContentViewHolder>() {

        private var mList: List<String>? = null

        @SuppressLint("NotifyDataSetChanged")
        fun setData(list: List<String>?) {
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
            val ubb = try {
                mList?.get(position)
            } catch (e: Exception) {
                null
            } ?: ""
            val binding: ItemLayoutContentBinding =
                holder.binding as? ItemLayoutContentBinding ?: return
            binding.contentView.setUBB(ubb)
        }

        override fun getItemCount(): Int {
            val count = mList?.size ?: 0
            return count
        }

    }

    inner class ContentViewHolder(itemBinding: ViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        val binding = itemBinding

    }
}