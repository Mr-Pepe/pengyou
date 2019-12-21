package com.mrpepe.pengyou.dictionary.searchView

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
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import com.mrpepe.pengyou.extractDefinitions

import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.activity_dictionary.view.*
import kotlinx.android.synthetic.main.content_dictionary.*

class SearchViewActivity : AppCompatActivity() {

    private lateinit var model: SearchViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setSupportActionBar(toolbar)

        model = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(SearchViewViewModel::class.java)

        val adapter =
            SearchResultAdapter({ entry: Entry ->
                entryClicked(entry)
            })

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

