package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout

class WordViewTabLayout: TabLayout {

    private var sumTabTitleWidths = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs : AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec : Int, heightMeasureSpec : Int) {

        // This implementation aligns the tab titles in the word view so that the first and last
        // tab title are aligned with the headword and HSK label

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tabMode = MODE_FIXED
            if (tabCount > 2) {
                if (sumTabTitleWidths == 0) {

                    // Calculate the cumulative of width of tab titles between the first and last tab
                    for (iTab in 1 until tabCount - 1) {
                        sumTabTitleWidths += getTabAt(iTab)?.view?.measuredWidth ?: 0
                    }

                    // Get the width of the TabLayout
                    var padding = measuredWidth - paddingStart - paddingEnd

                    // Subtract width of the first and last tab
                    padding -= getTabAt(0)?.view?.measuredWidth ?: 0
                    padding -= getTabAt(tabCount - 1)?.view?.measuredWidth ?: 0

                    // Subtract the widths of the tab titles between first and last tab
                    padding -= sumTabTitleWidths

                    // Calculate the padding that each tab should get to stretch out to the full
                    // tablayout width
                    padding /= (tabCount - 2)

                    // Padding left/right
                    padding /= 2

                    for (iTab in 1 until tabCount - 1) {
                        getTabAt(iTab)?.view?.setPadding(padding, 0, padding, 0)
                    }

                }
            }

            // Make the TabLayout wider or it uses some weird scrolling when selecting
            // the right-most tab
            this.layoutParams = LinearLayout.LayoutParams(measuredWidth + 1000, measuredHeight)
            tabMode = MODE_SCROLLABLE
        }
        else {
            tabMode = MODE_AUTO
        }
    }
}
