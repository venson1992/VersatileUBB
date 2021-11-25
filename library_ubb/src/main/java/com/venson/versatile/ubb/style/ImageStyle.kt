package com.venson.versatile.ubb.style

import android.content.ContentResolver
import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.Px
import com.bumptech.glide.Glide
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.bean.ViewHolderType
import com.venson.versatile.ubb.ext.getRealPath
import com.venson.versatile.ubb.ext.scale
import com.venson.versatile.ubb.holder.ImageViewHolder
import com.venson.versatile.ubb.span.ImageSpan
import com.venson.versatile.ubb.utils.convertHTML
import com.venson.versatile.ubb.widget.UBBContentView
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
        private const val TAG_NAME = "img"

        const val ATTR_WIDTH = "width"
        const val ATTR_HEIGHT = "height"
        const val ATTR_SRC = "src"

        const val VALUE_AUTO = "auto"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getImageSpan(context: Context, @Px maxWidth: Int): ImageSpan? {
        return withContext(Dispatchers.IO) {
            val src = getAttr(ATTR_SRC) as? String ?: ""
            val uri = Uri.parse(src)
            val realPath = uri.getRealPath(context)
            UBB.log(TAG, "getImageSpan=$realPath")
            if (realPath.isNullOrEmpty()) {
                return@withContext null
            }
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(realPath)
                .submit()
                .get() ?: return@withContext null
            val scaleBitmap = bitmap.scale(maxWidth)
            return@withContext ImageSpan(
                context,
                scaleBitmap,
                this@ImageStyle
            )
        }
    }

    override fun getHelper(): AbstractStyle.Helper {
        return Helper
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

    override fun getViewHolder(parent: ViewGroup): UBBContentView.ViewHolder? {
        return ImageViewHolder.build(parent.context)
    }

    object Helper : AbstractStyle.Helper() {

        override fun getTagName(): String {
            return TAG_NAME
        }

        override fun getViewHolderType(): Int {
            return ViewHolderType.VIEW_IMAGE.type
        }

        override fun convertUBB(source: String): String {
            val tagStart = "<${getTagName()} " +
                    "$ATTR_WIDTH=100%; " +
                    "$ATTR_HEIGHT=$VALUE_AUTO; " +
                    "alt=\"\" " +
                    "$ATTR_SRC=\""
            val tagEnd = "\"></${getTagName()}>"
            var dest = source
            dest = convertHTML(
                dest,
                getTagName(),
                getTagName(),
                tagStart,
                tagEnd
            )
            dest = convertHTML(
                dest,
                "${getTagName()}=,(.+?)",
                getTagName(),
                tagStart,
                tagEnd
            )
            return dest
        }

        override fun fromUBB(node: Node?, align: Paint.Align): AbstractStyle {
            return ImageStyle().also {
                if (node == null) {
                    return@also
                }
                it.putAttr(ATTR_WIDTH, node.attr(ATTR_WIDTH))
                it.putAttr(ATTR_HEIGHT, node.attr(ATTR_HEIGHT))
                val src = node.attr(ATTR_SRC)
                it.putAttr(ATTR_SRC, UBB.getImageEngine()?.getPath(src) ?: src)
            }
        }

    }
}