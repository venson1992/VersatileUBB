package com.venson.versatile.ubb.convert

import android.content.Context

/**
 * 简单解析器，不解析node层级
 */
class UBBSimpleConvert(context: Context) : AbstractConvert(context) {

    override fun isOnlyText(): Boolean {
        return true
    }

    override fun onParseStart() {

    }

    override fun onParseComplete() {

    }
}