package com.mrpepe.pengyou.dictionary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.extractDefinitions

import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.activity_dictionary.view.*
import kotlinx.android.synthetic.main.content_dictionary.*

class DictionaryActivity : AppCompatActivity() {

    private lateinit var model: DictionaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setSupportActionBar(toolbar)

        model = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(DictionaryViewModel::class.java)

        val adapter = SearchResultAdapter({ entry: Entry -> entryClicked(entry) })

        dictionaryResultList.layoutManager = LinearLayoutManager(this)
        dictionaryResultList.adapter = adapter

        model.searchResults.observe(this, Observer { searchResults ->
            searchResults?.let { adapter.setEntries(searchResults) }
        })

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText!!.isNotBlank()) {
                    model.searchFor(newText)
                } else {
                    adapter.setEntries(emptyList<Entry>())
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

class SearchResultAdapter(private val clickListener: (Entry) -> Unit) :
    RecyclerView.Adapter<ResultViewHolder>() {

    private var searchResults = emptyList<Entry>()

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
        holder.bind(this.searchResults[position], clickListener)
    }

    override fun getItemCount(): Int {
        return this.searchResults.size
    }

    internal fun setEntries(searchResults: List<Entry>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
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

