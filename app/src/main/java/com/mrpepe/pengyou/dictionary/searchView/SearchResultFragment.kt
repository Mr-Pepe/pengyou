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
            model = ViewModelProvider(this).get(SearchViewFragmentViewModel::class.java)
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

        model.searchResults.observe(viewLifecycleOwner, Observer { searchResults ->
            searchResults?.let { adapter.setEntries(searchResults) }
            resultCount.text = when(searchResults.size) {
                0 -> ""
                else -> "Appears in ${searchResults.size} words"
            }
        })

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

        val intent = Intent(activity, WordViewActivity::class.java)
        intent.putExtra("entry", entry)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun updateSearchResults(mode: UpdateSearchResultsMode) {
        if (mode != UpdateSearchResultsMode.IDLE) {
            when (model.searchQuery.isBlank()) {
                true -> {
                    resultCount.text = when (model.searchHistory.value?.size) {
                        0 -> ""
                        else -> "Recently viewed: " + model.searchHistory.value?.size.toString()
                    }
                    adapter.setEntries(model.searchHistory.value!!)
                }
                false -> {
                    resultCount.text = when (model.searchResults.value?.size) {
                        0 -> "Search results: 0"
                        in 1..999 -> "Search results: " + model.searchResults.value?.size.toString()
                        else -> "Search results: 999+"
                    }
                    adapter.setEntries(model.searchResults.value!!)
                }
            }

            if (mode == UpdateSearchResultsMode.SNAPTOTOP)
                searchResultList.scrollToPosition(0)
        }
    }
}
