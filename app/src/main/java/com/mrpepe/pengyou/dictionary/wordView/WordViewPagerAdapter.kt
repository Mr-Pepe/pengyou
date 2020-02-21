package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.R

class WordViewPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val tabIcons = arrayOf(
        R.drawable.ic_dictionary,
        R.drawable.ic_strokes,
        R.drawable.ic_decomposition,
        R.drawable.ic_brush_line
    )

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> WordDefinitionFragment()
            1 -> StrokeOrderFragment()
            2 -> DecompositionFragment()
            3 -> WordsContainingFragment()
            else -> WordDefinitionFragment()
        }
    }

    override fun getCount(): Int {
        return tabIcons.size
    }
}
