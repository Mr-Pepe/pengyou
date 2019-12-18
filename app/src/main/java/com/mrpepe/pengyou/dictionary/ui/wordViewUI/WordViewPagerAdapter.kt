package com.mrpepe.pengyou.dictionary.ui.wordViewUI

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mrpepe.pengyou.R

private val TAB_TITLES = arrayOf(
    R.string.tab_definition_text,
    R.string.tab_strokes_text,
    R.string.tab_components_text,
    R.string.tab_words_text,
    R.string.tab_usage_text,
    R.string.tab_sentences_text
)

class WordViewPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> WordDefinitionFragment()
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