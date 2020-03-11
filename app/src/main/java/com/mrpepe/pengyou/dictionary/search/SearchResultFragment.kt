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
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewFragmentDirections
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_search_result_list.*
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class SearchResultFragment : Fragment() {

    private lateinit var dictionaryViewModel: DictionarySearchViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView
    private lateinit var adapter : SearchResultAdapter
    private var lastClickTime: Long = 0

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
        return inflater.inflate(R.layout.fragment_search_result_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchResultList = view.searchResultList
        resultCount = view.resultCount

        adapter =
            SearchResultAdapter{ entry: Entry ->
                entryClicked(entry)
            }

        searchResultList.layoutManager = LinearLayoutManager(activity)
        searchResultList.adapter = adapter

        dictionaryViewModel.requestedLanguage.observe(viewLifecycleOwner, Observer {
            updateSearchResults()
        })

        dictionaryViewModel.englishSearchResults.observe(viewLifecycleOwner, Observer {
            updateSearchResults()
        })

        dictionaryViewModel.chineseSearchResults.observe(viewLifecycleOwner, Observer {
            updateSearchResults()
        })

        dictionaryViewModel.searchHistoryEntries.observe(viewLifecycleOwner, Observer {
            updateSearchResults()
        })

        dictionaryViewModel.newSearchLive.observe(viewLifecycleOwner, Observer {
            if (dictionaryViewModel.newSearch) {
                searchResultList.scrollToPosition(0)
                dictionaryViewModel.newSearch = false
            }
        })
    }

    private fun entryClicked(entry: Entry){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        hideKeyboard()

        findNavController().navigate(WordViewFragmentDirections.globalOpenWordViewAction(entry))
    }

    private fun updateSearchResults() {
        when (dictionaryViewModel.searchQuery.isBlank()) {

            // Show history
            true -> {
                when (dictionaryViewModel.searchHistoryIDs.size) {
                    0 -> {
                        resultCount.text = getString(R.string.no_history)
                        clearHistoryLink.text = ""
                    }
                    else -> {
                        resultCount.text = getString(R.string.history) + dictionaryViewModel.searchHistoryIDs.size.toString()
                        clearHistoryLink.movementMethod = LinkMovementMethod.getInstance()

                        val clearHistoryText = getString(R.string.clear_history).toSpannable()
                        clearHistoryText.setSpan(object: ClickableSpan() {
                            override fun onClick(widget: View) {
                                SearchHistory.clear()
                            }
                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = false
                            }
                        }, 0, clearHistoryText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        clearHistoryLink.text = SpannableStringBuilder().append(clearHistoryText)
                    }
                }
                adapter.setEntries(dictionaryViewModel.searchHistoryEntries.value ?: listOf())
                dictionaryViewModel.displayedLanguage.value = dictionaryViewModel.requestedLanguage.value
                searchResultList.scrollToPosition(0)
            }

            // Show search results
            false -> {
                clearHistoryLink.text = ""
                if (dictionaryViewModel.requestedLanguage.value == DictionarySearchViewModel.SearchLanguage.ENGLISH) {

                    when (dictionaryViewModel.englishSearchResults.value != null && dictionaryViewModel.englishSearchResults.value?.isNotEmpty()!!) {
                        true -> setEnglish()
                        false -> {
                            if (dictionaryViewModel.chineseSearchResults.value != null && dictionaryViewModel.chineseSearchResults.value?.isNotEmpty()!! || dictionaryViewModel.displayedLanguage.value == DictionarySearchViewModel.SearchLanguage.CHINESE) {
                                setChinese()
                            } else {
                                setEnglish()
                            }
                        }
                    }

                }
                else if (dictionaryViewModel.requestedLanguage.value == DictionarySearchViewModel.SearchLanguage.CHINESE) {

                    when (dictionaryViewModel.chineseSearchResults.value != null && dictionaryViewModel.chineseSearchResults.value?.isNotEmpty()!!) {
                        true -> setChinese()
                        false -> {
                            if (dictionaryViewModel.englishSearchResults.value != null && dictionaryViewModel.englishSearchResults.value?.isNotEmpty()!! || dictionaryViewModel.displayedLanguage.value == DictionarySearchViewModel.SearchLanguage.ENGLISH) {
                                setEnglish()
                            } else {
                                setChinese()
                            }
                        }
                    }

                }

                resultCount.text = when (adapter.searchResults.size) {
                    0 -> getString(R.string.search_results) + 0.toString()
                    in 1..MainApplication.MAX_SEARCH_RESULTS -> getString(R.string.search_results) + adapter.searchResults.size.toString()
                    else -> getString(R.string.search_results) + (MainApplication.MAX_SEARCH_RESULTS-1).toString() + "+"
                }
            }
        }
    }

    private fun setEnglish() {
        adapter.setEntries(dictionaryViewModel.englishSearchResults.value?.sortedWith(
            compareBy({it.priority})
        ) ?: listOf())
        dictionaryViewModel.displayedLanguage.value = DictionarySearchViewModel.SearchLanguage.ENGLISH
    }

    private fun setChinese() {
        adapter.setEntries(dictionaryViewModel.chineseSearchResults.value?.sortedWith(
            compareBy({it.wordLength}, {it.hsk}, {it.pinyinLength}, {it.priority})
        ) ?: listOf())
        dictionaryViewModel.displayedLanguage.value = DictionarySearchViewModel.SearchLanguage.CHINESE
    }
}
