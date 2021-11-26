package com.venson.versatile.ubb.ext

fun CharSequence.isEndBreakLine(): Boolean {
    if (isEmpty()) {
        return false
    }
    if (get(length - 1) == '\n') {
        return true
    }
    return false
}