package com.venson.versatile.ubb.fix

interface ISpanTouchFix {
    /**
     * 记录当前 Touch 事件对应的点是不是点在了 span 上面
     */
    fun setTouchSpanHit(hit: Boolean)
}