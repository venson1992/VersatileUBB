package com.venson.versatile.ubb.demo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataBean(
    var title: String? = null,
    var content: String? = null
) : Parcelable