package com.mrpepe.pengyou.dictionary.search

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class SearchResultFragment : Fragment() {

    private lateinit var dictionaryViewModel: DictionarySearchViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView
    private lateinit var adapter : SearchResultAdapter
    private var lastClickTime: Long = 0

    private var searchHistory = SearchHistory

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
            updateSearchResults(true)
        })

        dictionaryViewModel.englishSearchResults.observe(viewLifecycleOwner, Observer {
            updateSearchResults(true)
        })

        dictionaryViewModel.chineseSearchResults.observe(viewLifecycleOwner, Observer {
            updateSearchResults(true)
        })

        dictionaryViewModel.searchHistoryEntries.observe(viewLifecycleOwner, Observer {
            updateSearchResults(true)
        })
    }

    private fun entryClicked(entry: Entry){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        searchHistory.addToHistory(entry.id.toString())

        hideKeyboard()

        val action =
            DictionarySearchFragmentDirections.dictionaryEntryClickedAction(entry)

        findNavController().navigate(action)
    }

    private fun updateSearchResults(snapToTop: Boolean) {
        when (dictionaryViewModel.searchQuery.isBlank()) {
            true -> {
                resultCount.text = when (dictionaryViewModel.searchHistoryIDs.size) {
                    0 -> ""
                    else -> "Recently viewed: " + dictionaryViewModel.searchHistoryIDs.size.toString()
                }
                adapter.setEntries(dictionaryViewModel.searchHistoryEntries.value ?: listOf())
                dictionaryViewModel.displayedLanguage.value = dictionaryViewModel.requestedLanguage.value
            }
            false -> {
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
                    0 -> "Search results: 0"
                    in 1..999 -> "Search results: " + adapter.searchResults.size.toString()
                    else -> "Search results: 999+"
                }
            }
        }

        if (snapToTop) {
            searchResultList.scrollToPosition(0)
        }
    }

    private fun setEnglish() {
        adapter.setEntries(dictionaryViewModel.englishSearchResults.value ?: listOf())
        dictionaryViewModel.displayedLanguage.value = DictionarySearchViewModel.SearchLanguage.ENGLISH
    }

    private fun setChinese() {
        adapter.setEntries(dictionaryViewModel.chineseSearchResults.value ?: listOf())
        dictionaryViewModel.displayedLanguage.value = DictionarySearchViewModel.SearchLanguage.CHINESE
    }
}
