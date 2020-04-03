package com.mrpepe.pengyou.dictionary.search

import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel.SearchLanguage.CHINESE
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel.SearchLanguage.ENGLISH
import com.mrpepe.pengyou.dictionary.wordView.WordViewFragmentDirections
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_search_result_list.*
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class SearchResultFragment : Fragment() {

    private lateinit var dictionaryViewModel : DictionarySearchViewModel
    private lateinit var searchResultList : RecyclerView
    private lateinit var resultCount : TextView
    private lateinit var adapter : SearchResultAdapter
    private var lastClickTime : Long = 0

    private var historyMode = true

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
        return inflater.inflate(R.layout.fragment_search_result_list, container, false)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchResultList = view.searchResultList
        resultCount = view.resultCount

        adapter =
                SearchResultAdapter { entry : Entry ->
                    entryClicked(entry)
                }

        searchResultList.layoutManager = LinearLayoutManager(activity)
        searchResultList.adapter = adapter

        dictionaryViewModel.searchQuery.observe(viewLifecycleOwner, Observer {
            historyMode = it.isBlank()
            if (historyMode) {
                showHistory()
            }
        })

        dictionaryViewModel.displayedLanguage.observe(viewLifecycleOwner, Observer {
            if (!historyMode) {
                updateSearchResults(it)
            }
        })

        dictionaryViewModel.searchHistoryEntries.observe(viewLifecycleOwner, Observer {
            if (historyMode) {
                showHistory()
            }
        })

        dictionaryViewModel.newSearchLive.observe(viewLifecycleOwner, Observer {
            if (dictionaryViewModel.newSearch) {
                searchResultList.scrollToPosition(0)
                dictionaryViewModel.newSearch = false
            }
        })
    }

    private fun entryClicked(entry : Entry) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        hideKeyboard()

        findNavController().navigate(WordViewFragmentDirections.globalOpenWordViewAction(entry))
    }

    private fun updateSearchResults(language : DictionarySearchViewModel.SearchLanguage) {
        var results = listOf<Entry>()

        if (language == ENGLISH) {
            results = dictionaryViewModel.englishResults.value ?: listOf()
        }
        else if (language == CHINESE) {
            results = dictionaryViewModel.chineseSearchResults.value ?: listOf()
        }

        if (results.isNotEmpty()) {
            searchResultListLinearLayout?.removeView(resultCountLinearLayout)
        }
        else {
            if (resultCountLinearLayout.parent == null) {
                searchResultListLinearLayout.addView(resultCountLinearLayout, 0)
                resultCountLinearLayout.removeView(clearHistoryLink)
            }
            resultCount.text = getString(R.string.no_results_found)
        }

        adapter.setEntries(results)
    }

    private fun showHistory() {
        if (resultCountLinearLayout.parent == null) {
            searchResultListLinearLayout.addView(resultCountLinearLayout, 0)

            if (clearHistoryLink.parent == null) {
                resultCountLinearLayout.addView(clearHistoryLink,1)
            }
        }

        when (dictionaryViewModel.searchHistoryIDs.size) {
            0    -> {
                resultCount.text = getString(R.string.no_history)
                clearHistoryLink.text = ""
            }

            else -> {
                resultCount.text =
                        getString(R.string.history) + dictionaryViewModel.searchHistoryIDs.size.toString()
                clearHistoryLink.movementMethod = LinkMovementMethod.getInstance()

                val clearHistoryText = getString(R.string.clear_history).toSpannable()

                clearHistoryText.setSpan(object : ClickableSpan() {
                    override fun onClick(widget : View) {
                        SearchHistory.clear()
                    }

                    override fun updateDrawState(ds : TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false
                    }
                }, 0, clearHistoryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                clearHistoryLink.text = SpannableStringBuilder().append(clearHistoryText)
            }
        }

        adapter.setEntries(dictionaryViewModel.searchHistoryEntries.value ?: listOf())
        searchResultList.scrollToPosition(0)
    }
}
