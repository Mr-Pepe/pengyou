package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R

class DrawBoard (context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private var mPaint = Paint()
    private var mPath = Path()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f

    var pathExposed = MutableLiveData<MutableList<MutableList<List<Float>>>>()

    private var path  = mutableListOf<MutableList<List<Float>>>()

    var isClear = true

    private var strokeColor = TypedValue()

    init {
        MainApplication.homeActivity.theme.resolveAttribute(R.attr.colorOnBackground, strokeColor, true)

        mPaint.apply {
            color = strokeColor.data
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8f
            isAntiAlias = true
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                actionDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> actionMove(x, y)
            MotionEvent.ACTION_UP -> actionUp()
        }

        invalidate()
        return true
    }

    private fun actionDown(x: Float, y: Float) {

        path.add(mutableListOf())

        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y

        path.last().add(listOf(x, y))
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        pathExposed.value = path

        isClear = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath, mPaint)
    }

    fun clearCanvas() {
        mPath.reset()
        invalidate()
        path = mutableListOf()
        isClear = true
    }
}
