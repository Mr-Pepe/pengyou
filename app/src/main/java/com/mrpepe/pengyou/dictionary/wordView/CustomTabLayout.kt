package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

class CustomTabLayout: TabLayout {

    private var sumTabTitleWidths = 0
    private var sumTabWidths = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec : Int, heightMeasureSpec : Int) {

        var padding = 0

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (tabCount > 2) {
            if (sumTabTitleWidths == 0) {
                for (iTab in 1 until tabCount - 1) {
                    sumTabTitleWidths += getTabAt(iTab)?.view?.measuredWidth ?: 0
                }

                padding = (measuredWidth - paddingStart - paddingEnd -
                        (getTabAt(0)?.view?.measuredWidth ?: 0) -
                        (getTabAt(tabCount - 1)?.view?.measuredWidth ?: 0) - sumTabTitleWidths) /
                        (tabCount - 2) / 2

                for (iTab in 1 until tabCount - 1) {
                    getTabAt(iTab)?.view?.setPadding(padding, 0, padding, 0)
                }
            }
        }
    }
}
