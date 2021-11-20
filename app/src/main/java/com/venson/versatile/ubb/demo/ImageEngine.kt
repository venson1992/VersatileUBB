package com.venson.versatile.ubb.demo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.venson.versatile.ubb.ImageEngine
import com.venson.versatile.ubb.ext.getRealPath

class ImageEngine(context: Context) : ImageEngine(context) {

    override fun getPath(path: String): String {
        if (path.startsWith("http", true)) {
            return path
        }
        if (path.startsWith(ContentResolver.SCHEME_FILE, true)) {
            return path.substring((ContentResolver.SCHEME_FILE + "://").length)
        }
//        if (path.startsWith(ContentResolver.SCHEME_ANDROID_RESOURCE, true)){
//
//        }
        return Uri.parse(path).getRealPath(context) ?: return "http://file.25game.com$path"
    }
}