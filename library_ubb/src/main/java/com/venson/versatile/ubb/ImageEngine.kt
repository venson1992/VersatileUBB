package com.venson.versatile.ubb

import android.content.ContentResolver
import android.net.Uri
import com.venson.versatile.ubb.ext.getRealPath

abstract class ImageEngine {

    fun getPath(path: String): String {
        if (path.startsWith("http", true)) {
            return path
        }
        if (path.startsWith(ContentResolver.SCHEME_FILE, true)) {
            return path.substring((ContentResolver.SCHEME_FILE + "://").length)
        }
//                if (path.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE, true)) {
//
//                }
        UBB.getApplicationContext()?.let { context ->
            Uri.parse(path).getRealPath(context)?.let { url ->
                return url
            }
        }
        return "${getSchema()}://${getDomain()}$path"
    }

    abstract fun getDomain(): String

    abstract fun getSchema(): String
}