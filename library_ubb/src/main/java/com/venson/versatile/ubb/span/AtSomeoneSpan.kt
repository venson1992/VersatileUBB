package com.venson.versatile.ubb.span

import android.graphics.Paint
import com.venson.versatile.ubb.UBB
import com.venson.versatile.ubb.style.AtSomeoneStyle

class AtSomeoneSpan(atSomeoneStyle: AtSomeoneStyle, align: Paint.Align = Paint.Align.LEFT) :
    AbstractLineEnableSpan(atSomeoneStyle, align, UBB.getClickableColor())