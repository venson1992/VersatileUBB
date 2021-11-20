package com.venson.versatile.ubb

import android.content.Context

abstract class ImageEngine(protected val context: Context) {

    abstract fun getPath(path: String): String
}