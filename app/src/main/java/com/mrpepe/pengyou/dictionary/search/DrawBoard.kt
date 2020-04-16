package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.getThemeColor
import kotlin.math.abs

class DrawBoard (context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private var mPaint = Paint()
    private var mPath = Path()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f

    private var lastClickedTime : Long = 0

    private var onDoubleClickListener: OnClickListener? = null

    var pathExposed = MutableLiveData<MutableList<MutableList<List<Float>>>>()

    private var path  = mutableListOf<MutableList<List<Float>>>()

    var isClear = true

    init {
        mPaint.apply {
            color = getThemeColor(R.attr.colorOnBackground)
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
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - lastClickedTime < 300) {
                    onDoubleClickListener?.onClick(this)
                }
                else {
                    actionUp()
                    lastClickedTime = System.currentTimeMillis()
                }
            }
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
        if (abs(mStartX - x) > 1 || abs(mStartY - y) > 1) {
            mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
            mCurX = x
            mCurY = y

            path.last().add(listOf(x, y))
            lastClickedTime = 0
        }
    }

    private fun actionUp() {
        if (abs(mStartX - mCurX) > 1 || abs(mStartY - mCurY) > 1) {
            mPath.lineTo(mCurX, mCurY)
            pathExposed.value = path
            isClear = false
            lastClickedTime = 0
        }
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

    fun setOnDoubleClickListener(listener: OnClickListener) {
        onDoubleClickListener = listener
    }
}
