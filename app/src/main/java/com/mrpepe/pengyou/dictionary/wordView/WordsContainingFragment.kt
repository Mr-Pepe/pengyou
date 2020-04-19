package com.mrpepe.pengyou.dictionary.wordView

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
import com.mrpepe.pengyou.copyHeadwordToClipboard
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.search.SearchResultAdapter
import kotlinx.android.synthetic.main.fragment_search_result_list.view.*

class WordsContainingFragment : Fragment() {

    private lateinit var model: WordViewViewModel
    private lateinit var searchResultList: RecyclerView
    private lateinit var resultCount: TextView
    private var lastClickTime : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragment?.let {
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

        resultCount.setPadding(
            resources.getDimension(R.dimen.medium_padding).toInt(),
            resources.getDimension(R.dimen.medium_padding).toInt(),
            resources.getDimension(R.dimen.small_padding).toInt(),
            resources.getDimension(R.dimen.small_padding).toInt())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            SearchResultAdapter({ entry : Entry -> entryClicked(entry) },
                                    { entry : Entry -> entryLongClicked(entry) })

        searchResultList.layoutManager = LinearLayoutManager(activity)
        searchResultList.adapter = adapter

        model.wordsContaining.observe(viewLifecycleOwner, Observer { wordsContaining ->
            adapter.setEntries(wordsContaining?.sortedWith(
                compareBy({it.hsk}, {it.wordLength}, {it.priority})
            ) ?: listOf())
            resultCount.text = when(wordsContaining.size) {
                0 -> getString(R.string.no_appearance_in_other_words)
                else -> getString(R.string.appears_in_other_words) + wordsContaining.size.toString()
            }
        })
    }

    private fun entryClicked(entry: Entry){
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        findNavController().navigate(WordViewFragmentDirections.globalOpenWordViewAction(entry))
    }

    private fun entryLongClicked(entry : Entry): Boolean {
        activity?.let { activity ->
            copyHeadwordToClipboard(activity, entry)
        }

        return true
    }

}
