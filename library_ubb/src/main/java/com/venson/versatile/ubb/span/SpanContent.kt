package com.venson.versatile.ubb.span

class SpanContent {
    private val attrMap = hashMapOf<String, String>()
    private var text: String = ""

    fun putAttr(key: String, value: String) {
        attrMap[key] = value
    }

    fun getAttr() = attrMap

    fun setText(text: String) {
        this.text = text
    }

    fun getText() = text

    /**
     * content转ubb
     */
    fun toUBB(tag: String): String {
        val ubb = StringBuilder()
        ubb.append("[$tag")
        if (attrMap.isNotEmpty()) {
            attrMap.forEach { entry ->
                ubb.append(" ${entry.key}=\"${entry.value}\"")
            }
        }
        ubb.append("]").append(text).append("[/$tag]")
        return ubb.toString()
    }
}