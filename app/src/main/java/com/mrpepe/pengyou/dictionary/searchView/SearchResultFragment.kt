package com.mrpepe.pengyou.dictionary.searchView

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.UpdateSearchResultsMode
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import com.mrpepe.pengyou.hideKeyboard
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class SearchResultFragment : Fragment() {

    private lateinit var model: SearchViewFragmentViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView
    lateinit var adapter : SearchResultAdapter
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProvider(it).get(SearchViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search_result_list, container, false)

        searchResultList = root.searchResultList
        resultCount = root.resultCount

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter =
            SearchResultAdapter{ entry: Entry ->
                entryClicked(entry)
            }

        searchResultList.layoutManager = LinearLayoutManager(activity)
        searchResultList.adapter = adapter

        model.updateSearchResults.observe(viewLifecycleOwner, Observer { mode ->
            updateSearchResults(mode)
        })
    }

    private fun entryClicked(entry: Entry){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        model.newHistoryEntry.value = entry.id.toString()

        hideKeyboard()

        val intent = Intent(activity, WordViewActivity::class.java)
        intent.putExtra("entry", entry)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun updateSearchResults(mode: UpdateSearchResultsMode) {

        when (model.searchQuery.isBlank()) {
            true -> {
                resultCount.text = when (model.searchHistory.value?.size) {
                    0 -> ""
                    else -> "Recently viewed: " + model.searchHistory.value?.size.toString()
                }
                adapter.setEntries(model.searchHistory.value!!)
                model.displayedLanguage.value = model.requestedLanguage
            }
            false -> {

                if (model.requestedLanguage == SearchViewViewModel.SearchLanguage.ENGLISH) {
                    when (model.englishSearchResults.value?.isNotEmpty()!!) {
                        true -> setEnglish()
                        false -> {
                            if (model.chineseSearchResults.value?.isNotEmpty()!!) {
                                setChinese()
                            }
                            else {
                                setEnglish()
                            }
                        }
                    }
                }
                else if (model.requestedLanguage == SearchViewViewModel.SearchLanguage.CHINESE) {
                    when (model.chineseSearchResults.value?.isNotEmpty()!!) {
                        true -> setChinese()
                        false -> {
                            if (model.englishSearchResults.value?.isNotEmpty()!!) {
                                setEnglish()
                            }
                            else {
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

        if (mode == UpdateSearchResultsMode.SNAPTOTOP)
            searchResultList.scrollToPosition(0)
    }

    private fun setEnglish() {
        adapter.setEntries(model.englishSearchResults.value!!)
        model.displayedLanguage.value = SearchViewViewModel.SearchLanguage.ENGLISH
    }

    private fun setChinese() {
        adapter.setEntries(model.chineseSearchResults.value!!)
        model.displayedLanguage.value = SearchViewViewModel.SearchLanguage.CHINESE
    }
}
