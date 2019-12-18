package com.mrpepe.pengyou.dictionary

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.ui.wordView.WordViewViewModel
import com.mrpepe.pengyou.extractDefinitions

import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.activity_dictionary.view.*
import kotlinx.android.synthetic.main.content_dictionary.*

class DictionaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setSupportActionBar(toolbar)

        var model: DictionaryViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(DictionaryViewModel::class.java)
        val viewManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        dictionaryResultList.apply{
            layoutManager = viewManager
            setHasFixedSize(true)
        }

        val searchResultsObserver = Observer<List<Entry>> { searchResults ->
            dictionaryResultList.adapter =
                SearchResultAdapter(
                    searchResults,
                    { entry: Entry -> entryClicked(entry) })
        }

        model.init()
        model.searchResults.observe(this, searchResultsObserver)

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText!!.isNotBlank()) {
                    model.searchFor(newText)
                } else {
                    model.resetSearchResults()
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionary_search_view.clearFocus()
                return true
            }
        })
    }

    private fun entryClicked(entry: Entry){
        val intent = Intent(this, WordViewActivity::class.java)
        intent.putExtra("entry", entry)
        startActivity(intent)
    }
}

class SearchResultAdapter(private val searchResults: List<Entry>, private val clickListener: (Entry) -> Unit) :
    RecyclerView.Adapter<ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.dictionary_search_result,
                    parent,
                    false
                ) as CardView
        )
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(searchResults[position], clickListener)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }
}

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.headword)
    private var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    private var definitions: TextView = itemView.findViewById(R.id.definitions)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {
        headword.text = entry.simplified
        pinyin.text = entry.pinyin
        definitions.text = extractDefinitions(entry.definitions, false)

        itemView.setOnClickListener { clickListener(entry) }
    }
}

