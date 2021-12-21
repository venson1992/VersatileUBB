package com.venson.versatile.ubb.style

import android.content.ContentResolver
import android.content.Context
import android.graphics.Paint
import android.net.Uri
import androidx.annotation.Px
import com.venson.versatile.ubb.R
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.ext.getRealPath
import com.venson.versatile.ubb.span.GlideImageSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Node

/**
 * Uri.toString(content://),[ContentResolver.SCHEME_CONTENT]
 * http://,
 * file://,[ContentResolver.SCHEME_FILE]
 * android.resource://[ContentResolver.SCHEME_ANDROID_RESOURCE]
 */
class ImageStyle : AbstractStyle() {

    companion object {
        private const val TAG = "ImageStyle"
        const val TAG_NAME = "img"

        const val ATTR_ALIGN = "align"
        const val ATTR_WIDTH = "width"
        const val ATTR_HEIGHT = "height"
        const val ATTR_SRC = "src"

        const val VALUE_AUTO = "auto"
    }

    /**
     * 获取路径
     */
    fun getPath(): String {
        return getAttr(ATTR_SRC)
    }

    /**
     * 获取真实路径
     */
    fun getRealPath(context: Context): String? {
        val src = getPath()
        val uri = Uri.parse(src)
        return uri.getRealPath(context)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getImageSpan(
        context: Context,
        @Px maxSize: Int,
        listener: GlideImageSpan.OnImageLoadSuccessListener? = null
    ): GlideImageSpan {
        return withContext(Dispatchers.IO) {
            return@withContext GlideImageSpan(
                context,
                this@ImageStyle,
                R.drawable.default_drawable,
                maxSize,
                listener
            )
        }
    }

    override fun getTagName(): String {
        return TAG_NAME
    }

    override fun getSpan(): Any? {
        return null
    }

    /**
     * 图片文本占一位
     */
    override fun getSpanText(): String {
        return " "
    }

    override fun fromUBB(node: Node?, align: Paint.Align) {
        super.fromUBB(node, align)
        node?.attr(ATTR_SRC)?.let { src ->
            putAttr(ATTR_SRC, UBB.getImageEngine()?.getPath(src) ?: src)
        }
    }
}