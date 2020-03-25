package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.mrpepe.pengyou.CustomViewPager
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.DictionaryBaseFragment
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_dictionary_search.*

class DictionarySearchFragment : DictionaryBaseFragment() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var dictionaryViewModel: DictionarySearchViewModel
    private var blockKeyboard = false

    private var selectedInputMethodIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            dictionaryViewModel = ViewModelProvider(it).get(DictionarySearchViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dictionary_search, container, false)
    }

    override fun onResume() {
        super.onResume()
        dictionaryViewModel.displayedLanguage.value?.let { updateModeSwitch(it, null, null) }
        dictionaryViewModel.englishSearchResults.value?.let { updateModeSwitch(null, it.size, null) }
        dictionaryViewModel.chineseSearchResults.value?.let { updateModeSwitch(null, null, it.size) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dictionaryViewModel.displayedLanguage.observe(viewLifecycleOwner, Observer { language ->
            updateModeSwitch(language, null, null)
        })

        val sectionsPagerAdapter = DictionarySearchPagerAdapter(childFragmentManager)
        val viewPager : CustomViewPager = dictionarySearchViewPager
        viewPager.adapter = sectionsPagerAdapter
        dictionarySearchInputMethodTabs.setupWithViewPager(viewPager)
        // Deactivate horizontal paging
        viewPager.togglePagingEnabled()

        // Set icons for the tabs
        for (iTab in 0 until dictionarySearchInputMethodTabs.tabCount) {
            val tab = LayoutInflater.from(activity).inflate(R.layout.tab_item, null)
            tab.findViewById<ImageView>(R.id.tab_icon).setImageResource(sectionsPagerAdapter.tabIcons[iTab])
            dictionarySearchInputMethodTabs.getTabAt(iTab)?.customView = tab
        }

        selectedInputMethodIndex = dictionarySearchInputMethodTabs.selectedTabPosition
        dictionarySearchLinearLayout.removeView(dictionarySearchInputMethodTabs)

        dictionarySearchSearchBox.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != dictionaryViewModel.searchQuery) {
                    dictionaryViewModel.searchQuery = newText!!
                    dictionaryViewModel.search()
                }

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionarySearchSearchBox.clearFocus()
                return true
            }
        })

        dictionarySearchSearchBox.setOnQueryTextFocusChangeListener( object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                when (hasFocus) {
                    true -> if (dictionarySearchInputMethodTabs?.parent == null) {
                        dictionarySearchInputMethodTabs?.let {
                            dictionarySearchLinearLayout.addView(it, 1)
                            it.getTabAt(selectedInputMethodIndex)?.select()
                            if (selectedInputMethodIndex == 0) {
                                showKeyboard()
                            }
                        }
                    }
                    false -> {
                        selectedInputMethodIndex = dictionarySearchInputMethodTabs.selectedTabPosition
                        dictionarySearchLinearLayout.removeView(dictionarySearchInputMethodTabs)
                    }
                }
            }
        })

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    // Keep keyboard hidden if desired or open it
                    if (blockKeyboard) {
                        blockKeyboard = false
                    }
                    else {
                        if (!MainApplication.keyboardVisible) {
                            showKeyboard()
                        }
                        dictionarySearchSearchBox.requestFocus()
                    }
                }
                else if (position == 1) {
                    dictionarySearchSearchBox.requestFocus()
                    hideKeyboard()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

        })

        dictionaryViewModel.englishSearchResults.observe(viewLifecycleOwner, Observer { results ->
            updateModeSwitch(
                null,
                results?.size,
                null)
        })

        dictionaryViewModel.chineseSearchResults.observe(viewLifecycleOwner, Observer { results ->
            updateModeSwitch(
                null,
                null,
                results?.size)
        })

        modeSwitchEnglish.setOnClickListener {
            if (dictionaryViewModel.displayedLanguage.value != DictionarySearchViewModel.SearchLanguage.ENGLISH) {
                requestToggleDisplayedLanguage()
            }
        }

        modeSwitchChinese.setOnClickListener {
            if (dictionaryViewModel.displayedLanguage.value != DictionarySearchViewModel.SearchLanguage.CHINESE) {
                requestToggleDisplayedLanguage()
            }
        }

        dictionarySearchSearchBox?.requestFocus()

    }

    private fun updateModeSwitch(
        language: DictionarySearchViewModel.SearchLanguage?,
        nEnglishResults: Int?,
        nChineseResults: Int?) {

        nEnglishResults?.let {
            modeSwitchEnglish.text = "ENGLISH  (${formatResultCount(it)})"
        }

        nChineseResults?.let {
            modeSwitchChinese.text = "中文  (${formatResultCount(it)})"
        }

        language?.let {language ->
            val nightMode = MainApplication.homeActivity.isNightMode()

            val activeColor = if (nightMode) R.color.darkThemeColorOnPrimary else R.color.lightThemeColorOnPrimary
            val inactiveColor = if (nightMode) R.color.darkThemeColorPrimaryDark else R.color.lightThemeColorPrimaryDark

            when (language) {
                DictionarySearchViewModel.SearchLanguage.ENGLISH -> {
                    activity?.let { activity -> modeSwitchEnglish.setTextColor(activity.getColor(activeColor)) }
                    activity?.let { activity -> modeSwitchChinese.setTextColor(activity.getColor(inactiveColor)) }
                }
                DictionarySearchViewModel.SearchLanguage.CHINESE -> {
                    activity?.let { activity -> modeSwitchEnglish.setTextColor(activity.getColor(inactiveColor)) }
                    activity?.let { activity -> modeSwitchChinese.setTextColor(activity.getColor(activeColor)) }
                }
            }
        }

    }

    private fun requestToggleDisplayedLanguage() {
        if (dictionaryViewModel.displayedLanguage.value != dictionaryViewModel.requestedLanguage.value &&
                dictionarySearchSearchBox.query.isNotBlank()) {
            val message = when (dictionaryViewModel.requestedLanguage.value) {
                DictionarySearchViewModel.SearchLanguage.CHINESE -> "No results for Chinese search available."
                DictionarySearchViewModel.SearchLanguage.ENGLISH -> "No results for English search available."
                else -> ""
            }
            Toast.makeText(MainApplication.homeActivity, message, Toast.LENGTH_SHORT).show()
        }
        else {
            dictionaryViewModel.toggleDisplayedLanguage()
        }
    }

    private fun formatResultCount(value: Int): String {
        return when (value) {
            in 0 until MainApplication.MAX_SEARCH_RESULTS -> value.toString()
            else -> (MainApplication.MAX_SEARCH_RESULTS-1).toString() + "+"
        }
    }

    fun showKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun addCharacterToQuery(newChar: String) {
        val previousQuery = dictionarySearchSearchBox.query
        dictionarySearchSearchBox.setQuery("$previousQuery$newChar", false)
    }

    fun deleteLastCharacterOfQuery() {
        dictionarySearchSearchBox.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }

    fun submitQuery() {
        blockKeyboard = true
        dictionarySearchViewPager.requestFocus()
        dictionarySearchInputMethodTabs.setScrollPosition(0, 0.toFloat(), true)
        dictionarySearchViewPager.setCurrentItem(0)
    }
}
