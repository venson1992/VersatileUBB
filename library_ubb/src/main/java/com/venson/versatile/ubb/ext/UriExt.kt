package com.venson.versatile.ubb.ext

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

fun Uri?.getRealPath(context: Context): String? {
    this ?: return null
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        && DocumentsContract.isDocumentUri(context, this)
    ) {
        if (isExternalStorageDocument()) {
            val docId: String = DocumentsContract.getDocumentId(this)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument()) {
            val id: String = DocumentsContract.getDocumentId(this)
            val contentUri: Uri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument()) {
            val docId: String = DocumentsContract.getDocumentId(this)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = MediaStore.Images.Media._ID + "=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    }
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            return uriToFileApiQ(context, this)
        }
        "content".equals(this.scheme, true) -> {
            if (isGooglePhotosUri()) {
                return lastPathSegment
            }
            return getDataColumn(context, this, null, null);
        }
        "file".equals(scheme, true) -> {
            return path
        }
        else -> return null
    }
}

/**
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun Uri?.isExternalStorageDocument(): Boolean {
    this ?: return false
    return "com.android.externalstorage.documents" == authority
}

/**
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun Uri?.isDownloadsDocument(): Boolean {
    this ?: return false
    return "com.android.providers.downloads.documents" == authority
}

/**
 * @return Whether the Uri authority is MediaProvider.
 */
fun Uri?.isMediaDocument(): Boolean {
    this ?: return false
    return "com.android.providers.media.documents" == authority
}

/**
 * @return Whether the Uri authority is Google Photos.
 */
fun Uri?.isGooglePhotosUri(): Boolean {
    this ?: return false
    return "com.google.android.apps.photos.content" == authority
}

private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    uri ?: return null
    var cursor: Cursor? = null
    val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor = context.contentResolver
            .query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}


/**
 * Android 10 以上适配 另一种写法
 * @param context
 * @param uri
 * @return
 */
private fun getFileFromContentUri(context: Context, uri: Uri?): String? {
    if (uri == null) {
        return null
    }
    val filePath: String
    val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        uri, filePathColumn, null, null, null
    )
    if (cursor != null) {
        cursor.moveToFirst()
        try {
            val index: Int = cursor.getColumnIndex(filePathColumn[0])
            filePath = cursor.getString(index)
            return filePath
        } catch (e: Exception) {
        } finally {
            cursor.close()
        }
    }
    return ""
}

/**
 * Android 10 以上适配
 * @param context
 * @param uri
 * @return
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
private fun uriToFileApiQ(context: Context, uri: Uri): String? {
    var file: File? = null
    //android10以上转换
    if (uri.scheme == ContentResolver.SCHEME_FILE) {
        uri.path?.let { path ->
            file = File(path)
        }
    } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        //把文件复制到沙盒目录
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor?.moveToFirst() == true) {
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val displayName = cursor.getString(columnIndex)
            try {
                contentResolver.openInputStream(uri)?.let { `is` ->
                    val cache = File(
                        context.externalCacheDir!!.absolutePath,
                        ((Math.random() + 1) * 1000).roundToInt().toString() + displayName
                    )
                    val fos = FileOutputStream(cache)
                    FileUtils.copy(`is`, fos)
                    file = cache
                    fos.close()
                    `is`.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        cursor?.close()
    }
    return file?.absolutePath
}