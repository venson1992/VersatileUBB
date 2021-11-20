package com.venson.versatile.ubb.ext

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * 调整图片大小
 * @param maxWidth  最大宽度
 * @param maxHeight  最大高度
 * @return Bitmap
 */
fun Bitmap.scale(maxWidth: Int = 0, maxHeight: Int = 0): Bitmap {
    val sourceWidth = width
    val sourceHeight = height
    var targetWidth = sourceWidth
    var targetHeight = sourceHeight
    if (maxWidth in 1 until targetWidth) {
        targetHeight = maxWidth.times(targetHeight).div(targetWidth)
        targetWidth = maxWidth
    }
    if (maxHeight in 1 until targetHeight) {
        targetWidth = targetWidth.times(maxHeight).div(targetHeight)
        targetHeight = maxHeight
    }
    val matrix = Matrix()
    matrix.postScale(
        targetWidth.toFloat().div(sourceWidth),
        targetHeight.toFloat().div(sourceHeight)
    )
    return Bitmap.createBitmap(
        this,
        0,
        0,
        sourceWidth,
        sourceHeight,
        matrix,
        true
    )
}