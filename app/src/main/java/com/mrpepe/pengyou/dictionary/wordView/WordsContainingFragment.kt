package com.mrpepe.pengyou.dictionary.wordView

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
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.search.SearchResultAdapter
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class WordsContainingFragment : Fragment() {

    private lateinit var model: WordViewViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView
    private var lastClickTime : Long = 0
    private var searchHistory = SearchHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProvider(it).get(WordViewViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search_result_list, container, false)

        searchResultList = root.searchResultList
        resultCount = root.resultCount

        val scale = getResources().getDisplayMetrics().density;

        resultCount.setPadding(
            resources.getDimension(R.dimen.medium_padding).toInt(),
            resources.getDimension(R.dimen.large_padding).toInt(),
            resources.getDimension(R.dimen.small_padding).toInt(),
            resources.getDimension(R.dimen.small_padding).toInt())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            SearchResultAdapter{ entry: Entry ->
                entryClicked(entry)
            }

        searchResultList.layoutManager = LinearLayoutManager(activity)
        searchResultList.adapter = adapter

        model.wordsContaining.observe(viewLifecycleOwner, Observer { wordsContaining ->
            wordsContaining?.let { adapter.setEntries(wordsContaining) }
            resultCount.text = when(wordsContaining.size) {
                0 -> ""
                else -> "Appears in ${wordsContaining.size} words"
            }
        })
    }

    private fun entryClicked(entry: Entry){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        searchHistory.addToHistory(entry.id.toString())

        // TODO: Open new word view

//        val intent = Intent(activity, WordViewActivity::class.java)
//        intent.putExtra("entry", entry)
//        startActivity(intent)
    }
}
