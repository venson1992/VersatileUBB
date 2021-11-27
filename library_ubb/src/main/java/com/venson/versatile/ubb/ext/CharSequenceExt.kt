package com.venson.versatile.ubb.ext

import com.venson.versatile.ubb.UBB

fun CharSequence.isEndBreakLine(): Boolean {
    if (isEmpty()) {
        return false
    }
    if (get(length - 1) == UBB.BREAK_LINE) {
        return true
    }
    return false
}