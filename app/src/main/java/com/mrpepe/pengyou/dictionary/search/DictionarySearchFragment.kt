package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.DictionaryBaseFragment
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel.SearchLanguage.CHINESE
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel.SearchLanguage.ENGLISH
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_dictionary_search.*
import kotlinx.android.synthetic.main.fragment_stroke_order.*

class DictionarySearchFragment : DictionaryBaseFragment() {
    private lateinit var dictionaryViewModel : DictionarySearchViewModel
    private var blockKeyboard = false

    private var isInHandwritingMode = false

    private var nEnglishResults = 0
    private var nChineseResults = 0

    private var displayedLanguage = CHINESE

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            dictionaryViewModel = ViewModelProvider(it).get(DictionarySearchViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dictionary_search, container, false)
    }

    override fun onResume() {
        super.onResume() //TODO
//        dictionaryViewModel.displayedLanguage.value?.let { updateSearchResults(it, null, null) }
//        dictionaryViewModel.englishResults.value?.let { updateSearchResults(null, it.size, null) }
//        dictionaryViewModel.chineseSearchResults.value?.let {
//            updateSearchResults(
//                null,
//                null,
//                it.size
//            )
//        }
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dictionaryViewModel.displayedLanguage.observe(viewLifecycleOwner, Observer { language ->
            updateModeSwitchColor(language)
            displayedLanguage = language
            updateResultCounts(true)
        })

        dictionarySearchSearchBox.setOnQueryTextListener(object :
                                                             android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText : String?) : Boolean {
                dictionaryViewModel.search(newText!!)

                return true
            }

            override fun onQueryTextSubmit(query : String?) : Boolean {
                if (isInHandwritingMode) {
                    submitQueryFromDrawboard()
                }
                dictionarySearchSearchBox.clearFocus()
                return true
            }
        })

        buttonHandwriting.setOnClickListener {
            toggleHandwritingMode()
        }

        dictionaryViewModel.chineseSearchResults.observe(viewLifecycleOwner, Observer {
            nChineseResults = it.size
            updateResultCounts(false)
        })

        dictionaryViewModel.englishResults.observe(viewLifecycleOwner, Observer {
            nEnglishResults = it.size
            updateResultCounts(false)
        })

        modeSwitchEnglish.setOnClickListener {
            if (isInHandwritingMode) {
                blockKeyboard = true
                toggleHandwritingMode()
            }

            dictionaryViewModel.requestLanguage(ENGLISH)
        }

        modeSwitchChinese.setOnClickListener {
            if (isInHandwritingMode) {
                blockKeyboard = true
                toggleHandwritingMode()
            }

            dictionaryViewModel.requestLanguage(CHINESE)
        }

        dictionarySearchSearchBox?.requestFocus()
    }

    private fun updateModeSwitchColor(language : DictionarySearchViewModel.SearchLanguage?) {
        language?.let {
            activity?.let { activity ->
                val nightMode = MainApplication.homeActivity.isNightMode()

                val activeColor =
                        activity.getColor(if (nightMode) R.color.darkThemeColorOnPrimary
                                          else R.color.lightThemeColorOnPrimary)
                val inactiveColor =
                        activity.getColor(if (nightMode) R.color.darkThemeColorPrimaryDark
                                          else R.color.lightThemeColorPrimaryDark)

                if (!isInHandwritingMode) {
                    when (language) {
                        ENGLISH -> {
                            modeSwitchEnglish.setTextColor(activeColor)
                            modeSwitchChinese.setTextColor(inactiveColor)
                        }

                        CHINESE -> {
                            modeSwitchEnglish.setTextColor(inactiveColor)
                            modeSwitchChinese.setTextColor(activeColor)
                        }
                    }
                }
                else {
                    modeSwitchEnglish.setTextColor(inactiveColor)
                    modeSwitchChinese.setTextColor(inactiveColor)
                }
            }
        }
    }

    private fun updateResultCounts(force : Boolean) {
        if (force || displayedLanguage != ENGLISH) {
            modeSwitchEnglish.text = "ENGLISH  (${formatResultCount(nEnglishResults)})"
        }

        if (force || displayedLanguage != CHINESE) {
            modeSwitchChinese.text = "中文  (${formatResultCount(nChineseResults)})"
        }
    }

    private fun toggleHandwritingMode() {
        if (isInHandwritingMode) {
            childFragmentManager.beginTransaction()
                .replace(R.id.dictionaryContainer, SearchResultFragment()).commit()
            isInHandwritingMode = false

            if (!blockKeyboard) {
                showKeyboard()
                blockKeyboard = false
            }
            else {
                hideKeyboard()
            }

//            buttonHandwriting.setColorFilter(getStrokeOrderControlEnabledColor(), PorterDuff.Mode.SRC_IN)
        }
        else {
            childFragmentManager.beginTransaction()
                .replace(R.id.dictionaryContainer, HandwritingFragment()).commit()
            isInHandwritingMode = true
            dictionarySearchSearchBox.requestFocus()
            hideKeyboard()
        }
        updateModeSwitchColor(displayedLanguage)
    }

    private fun formatResultCount(value : Int) : String {
        return when (value) {
            in 0 until MainApplication.MAX_SEARCH_RESULTS -> value.toString()
            else                                          -> (MainApplication.MAX_SEARCH_RESULTS - 1).toString() + "+"
        }
    }

    private fun showKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun addCharacterToQuery(newChar : String) {
        val previousQuery = dictionarySearchSearchBox?.query
        dictionarySearchSearchBox?.setQuery("$previousQuery$newChar", false)
    }

    fun deleteLastCharacterOfQuery() {
        dictionarySearchSearchBox.dispatchKeyEvent(
            KeyEvent(
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DEL
            )
        )
    }

    fun submitQueryFromDrawboard() {
        blockKeyboard = true
        toggleHandwritingMode()
    }
}
