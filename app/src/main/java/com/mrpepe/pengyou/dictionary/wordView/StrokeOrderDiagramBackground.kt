package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_word_view.view.*

class StrokeOrderDiagramBackground: View {

    var paint = Paint()

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs : AttributeSet) : super(context,attrs){
        init()
    }

    constructor(context: Context,  attrs: AttributeSet , defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        init()
    }

    fun init() {
        val strokeColor = TypedValue()
        paint.isAntiAlias = true
        MainApplication.homeActivity.theme.resolveAttribute(R.attr.colorControlNormal, strokeColor, true)
        paint.color = when (MainApplication.homeActivity.isNightMode()) {
            true -> Color.DKGRAY
            false -> Color.LTGRAY + 0x222222
        }
        paint.strokeWidth = 5F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val offset = 50F

        val start = height.toFloat() * 0.05F
        val stopY = height.toFloat() * 0.95F
        val stopX = width.toFloat() * 0.95F

        // Bounding box
        canvas?.drawLine(start, start, start, stopY, paint)
        canvas?.drawLine(start, start, stopX, start, paint)
        canvas?.drawLine(start, stopY, stopX, stopY, paint)
        canvas?.drawLine(stopX, start, stopX, stopY, paint)

        // Diagonals
        canvas?.drawLine(start, start, stopX, stopY, paint)
        canvas?.drawLine(stopX, start, start, stopY, paint)

//        canvas?.drawLine(0F, 0F, height.toFloat(), width.toFloat(), paint)
//        canvas?.drawLine(0F, 0F, height.toFloat(), width.toFloat(), paint)
    }
}
