package com.mrpepe.pengyou.dictionary.wordView

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.searchView.SearchResultAdapter
import kotlinx.android.synthetic.main.search_result_list.view.*
import java.lang.Exception

class WordsContainingFragment : Fragment() {

    private lateinit var model: WordViewFragmentViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProviders.of(it).get(WordViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.search_result_list, container, false)

        searchResultList = root.searchResultList
        resultCount = root.resultCount

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

        model.wordsContaining.observe(this, Observer { wordsContaining ->
            wordsContaining?.let { adapter.setEntries(wordsContaining) }
            resultCount.text = when(wordsContaining.size) {
                0 -> ""
                else -> "Appears in ${wordsContaining.size} words"
            }
        })
    }

    private fun entryClicked(entry: Entry){
        val intent = Intent(activity, WordViewActivity::class.java)
        intent.putExtra("entry", entry)
        startActivity(intent)
    }
}
