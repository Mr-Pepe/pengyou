package com.mrpepe.pengyou.dictionary.wordView

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R

class WordViewPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabLabels = arrayOf(
        MainApplication.getContext().getString(R.string.definition_tab),
        MainApplication.getContext().getString(R.string.stroke_tab),
        MainApplication.getContext().getString(R.string.word_tab)
    )

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> WordDefinitionFragment()
            1 -> StrokeOrderFragment()
            2 -> WordsContainingFragment()
            else -> WordDefinitionFragment()
        }
    }

    override fun getCount(): Int {
        return tabLabels.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabLabels[position]
    }
}
