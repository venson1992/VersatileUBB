package com.venson.versatile.ubb.demo.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class HomeViewModel : ViewModel() {

    val mDataList: MutableLiveData<List<String>?> = MutableLiveData()

    fun readTestData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            mDataList.postValue(null)
            val stringBuilder = StringBuilder()
            try {
                val assetManager = context.assets
                val bf = BufferedReader(InputStreamReader(assetManager.open("test.json")))
                var line: String?
                while (bf.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val contentList = mutableListOf<String>()
            try {
                val jsonArray = JSONArray(stringBuilder.toString())
                for (index in 0 until jsonArray.length()) {
                    try {
                        val jsonObject = jsonArray.optJSONObject(index)
                        val content = jsonObject.optString("Content")
                        contentList.add(content)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mDataList.postValue(contentList)
        }
    }
}