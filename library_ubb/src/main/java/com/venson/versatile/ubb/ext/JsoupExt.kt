package com.venson.versatile.ubb.ext

import org.jsoup.nodes.Node

fun Node.getText(): String {
    val childSize = childNodeSize()
    var text = if (childSize > 0) {
        val stringBuilder = StringBuilder()
        for (index in 0 until childSize) {
            try {
                stringBuilder.append(childNode(index).toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stringBuilder.toString()
    } else {
        toString()
    }
    text = text.replace("\n", "")
    text = text.replace("<br></br>", "\n", true)
    return text
}