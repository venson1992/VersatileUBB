package com.venson.versatile.ubb.convert

import android.widget.EditText

/**
 * 针对UBBEditText的UBB解析器
 */
open class UBBEditTextConvert(private val editText: EditText) :
    AbstractConvert(editText.context) {

    override fun isOnlyText(): Boolean {
        return false
    }

    override fun onParseStart() {
        editText.setText("")
    }

    override fun onParseComplete() {
        editText.text = getSpannableString()
    }

}