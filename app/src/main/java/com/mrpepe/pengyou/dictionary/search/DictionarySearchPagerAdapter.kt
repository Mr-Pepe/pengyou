package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.R

class DictionarySearchPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val tabIcons = arrayOf(
        R.drawable.ic_keyboard_line,
        R.drawable.ic_brush_line
    )

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SearchResultFragment()
            else -> HandwritingFragment()
        }
    }

    override fun getCount(): Int {
        return tabIcons.size
    }


}
