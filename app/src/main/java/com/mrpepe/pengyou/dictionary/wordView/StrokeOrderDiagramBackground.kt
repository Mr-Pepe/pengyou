package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.preference.PreferenceManager
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.getThemeColor

class StrokeOrderDiagramBackground: View {

    private var paint = Paint()

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
        paint.isAntiAlias = true
        paint.color = getThemeColor(R.attr.strokeOrderControlButtonsBackgroundColor)
        paint.strokeWidth = 5F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val preferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext())

        val backgroundSettings = preferences.getStringSet("stroke_background", resources
            .getStringArray(R.array.preferences_stroke_order_background_default_values).toSet())

        val start = height.toFloat() * 0.02F
        val stopY = height.toFloat() * 0.98F
        val stopX = width.toFloat() * 0.98F

        // Bounding box
        if (backgroundSettings?.contains(resources.getString(R.string
                                                                 .stroke_order_diagram_background_outline)) == true) {
            canvas?.drawLine(start, start, start, stopY, paint)
            canvas?.drawLine(start, start, stopX, start, paint)
            canvas?.drawLine(start, stopY, stopX, stopY, paint)
            canvas?.drawLine(stopX, start, stopX, stopY, paint)
        }

        // Cross
        if (backgroundSettings?.contains(resources.getString(R.string
                                                                 .stroke_order_diagram_background_cross)) == true) {
            canvas?.drawLine(width.toFloat() / 2, start, width.toFloat() / 2, stopY,
                             paint)
            canvas?.drawLine(start, height.toFloat() / 2, stopX, height.toFloat() / 2,
                             paint)
        }

        // Diagonals
        if (backgroundSettings?.contains(resources.getString(R.string
                                                                 .stroke_order_diagram_background_diagonals)) == true) {
            canvas?.drawLine(start, start, stopX, stopY, paint)
            canvas?.drawLine(stopX, start, start, stopY, paint)
        }

    }
}
