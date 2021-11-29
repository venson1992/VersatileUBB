package com.venson.versatile.ubb.bean

enum class UBBViewType(val tagName: String, val type: Int) {
    VIEW_TEXT("text", 0),
    VIEW_IMAGE("img", 1),
    VIEW_AUDIO("audio", 2),
    VIEW_VIDEO("video", 3)
}