package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.graphics.ColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.DictionaryBaseFragment

import kotlinx.android.synthetic.main.fragment_dictionary_search.*
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchInputMethodTabs
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchSearchBox
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchViewPager
import java.util.*
import kotlin.concurrent.timerTask

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
        dictionaryViewModel.displayedLanguage.value?.let { setModeSwitchIcon(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dictionarySearchToolbar.inflateMenu(R.menu.dictionary_search_menu)
        modeSwitch = dictionarySearchToolbar.menu.getItem(0)

        dictionaryViewModel.displayedLanguage.observe(viewLifecycleOwner, Observer { language ->
            setModeSwitchIcon(language)
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

        dictionarySearchToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.modeSwitch -> {
                        if (dictionaryViewModel.displayedLanguage.value != dictionaryViewModel.requestedLanguage.value &&
                                dictionarySearchSearchBox.query.isNotBlank()) {
                            val message = when (dictionaryViewModel.requestedLanguage.value) {
                                DictionarySearchViewModel.SearchLanguage.CHINESE -> "No results for Chinese search available."
                                DictionarySearchViewModel.SearchLanguage.ENGLISH -> "No results for English search available."
                                else -> ""
                            }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        }
                        else {
                            dictionaryViewModel.toggleDisplayedLanguage()
                        }

                        true
                    }
                        else -> true
                }
            }
        })

        dictionarySearchSearchBox?.requestFocus()
    }

    fun setModeSwitchIcon(language: DictionarySearchViewModel.SearchLanguage) {
        when (language) {
            DictionarySearchViewModel.SearchLanguage.ENGLISH -> {
                modeSwitch.icon = ContextCompat.getDrawable(
                    MainApplication.getContext(),
                    R.drawable.ic_english_mode
                )

            }
            DictionarySearchViewModel.SearchLanguage.CHINESE -> {
                modeSwitch.icon = ContextCompat.getDrawable(
                    MainApplication.getContext(),
                    R.drawable.ic_chinese_mode
                )

            }
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
