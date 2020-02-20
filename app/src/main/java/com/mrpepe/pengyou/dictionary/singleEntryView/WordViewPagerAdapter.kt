package com.mrpepe.pengyou.dictionary.singleEntryView

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.PlaceholderFragment
import com.mrpepe.pengyou.R

class WordViewPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val tabIcons = arrayOf(
        R.drawable.ic_dictionary_line,
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
            else -> PlaceholderFragment.newInstance(position + 1)
        }
    }

    override fun getCount(): Int {
        return tabIcons.size
    }
}
