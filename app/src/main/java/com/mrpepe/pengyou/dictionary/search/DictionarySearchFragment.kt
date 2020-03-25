package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.DictionaryBaseFragment
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_dictionary_search.*

class DictionarySearchFragment : DictionaryBaseFragment() {
    private lateinit var dictionaryViewModel: DictionarySearchViewModel
    private var blockKeyboard = false

    private var isInHandwritingMode = false

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

        buttonHandwriting.setOnClickListener {
            toggleHandwritingMode()
        }

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
            if (isInHandwritingMode) {
                blockKeyboard = true
                toggleHandwritingMode()
            }

            if (dictionaryViewModel.displayedLanguage.value != DictionarySearchViewModel.SearchLanguage.ENGLISH) {
                requestToggleDisplayedLanguage()
            }
        }

        modeSwitchChinese.setOnClickListener {
            if (isInHandwritingMode) {
                blockKeyboard = true
                toggleHandwritingMode()
            }

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
                else -> {
                    activity?.let { activity -> modeSwitchEnglish.setTextColor(activity.getColor(inactiveColor)) }
                    activity?.let { activity -> modeSwitchChinese.setTextColor(activity.getColor(inactiveColor)) }
                }
            }
        }

    }

    private fun toggleHandwritingMode() {
        if (isInHandwritingMode) {
            childFragmentManager.beginTransaction().replace(R.id.dictionaryContainer, SearchResultFragment()).commit()
            isInHandwritingMode = false

            if (!blockKeyboard) {
                showKeyboard()
                blockKeyboard = false
            }
            else {
                hideKeyboard()
            }
        }
        else {
            childFragmentManager.beginTransaction().replace(R.id.dictionaryContainer, HandwritingFragment()).commit()
            isInHandwritingMode = true
            updateModeSwitch(DictionarySearchViewModel.SearchLanguage.NONE, null, null)
            dictionarySearchSearchBox.requestFocus()
            hideKeyboard()
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

    fun submitQueryFromDrawboard() {
        blockKeyboard = true
        toggleHandwritingMode()
    }
}
