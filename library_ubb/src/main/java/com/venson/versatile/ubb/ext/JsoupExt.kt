package com.venson.versatile.ubb.ext

import com.venson.versatile.ubb.UBB
import org.jsoup.nodes.Node

fun Node.getText(): String {
    val childSize = childNodeSize()
    var text = if (childSize > 0) {
        val stringBuilder = StringBuilder()
        for (index in 0 until childSize) {
            try {
                stringBuilder.append(childNode(index).getText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stringBuilder.toString()
    } else {
        toString()
    }
    text = text.replace(UBB.BREAK_LINE.toString(), "")
    text = text.replace("<br></br>", UBB.BREAK_LINE.toString(), true)
    return text
}