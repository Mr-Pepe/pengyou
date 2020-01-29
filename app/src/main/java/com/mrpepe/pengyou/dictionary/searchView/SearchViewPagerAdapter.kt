package com.mrpepe.pengyou.dictionary.searchView

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.PlaceholderFragment
import com.mrpepe.pengyou.R

class SearchViewPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    private val TAB_TITLES = arrayOf(
        R.string.tab_keyboard,
        R.string.tab_handwriting
    )

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SearchResultFragment()
            else -> PlaceholderFragment.newInstance(position + 1)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}
