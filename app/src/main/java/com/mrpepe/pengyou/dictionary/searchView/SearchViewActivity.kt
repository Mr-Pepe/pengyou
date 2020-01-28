package com.mrpepe.pengyou.dictionary.searchView

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrpepe.pengyou.BaseActivity
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity

import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.activity_search_view.view.*
import kotlinx.android.synthetic.main.search_result_list.*

class SearchViewActivity : BaseActivity() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var model: SearchViewViewModel
    private var searchHistory = listOf<Entry>()
    private var searchResults = listOf<Entry>()
    lateinit var adapter : SearchResultAdapter

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.that_menu, menu)
        modeSwitch = menu?.getItem(0)!!

        model.searchLanguage.observe(this, Observer { mode ->
            when (mode) {
                SearchViewViewModel.SearchLanguage.ENGLISH -> {
                    modeSwitch.icon = ContextCompat.getDrawable(getApplicationContext(),R.drawable.e);
                }
                SearchViewViewModel.SearchLanguage.CHINESE -> {
                    modeSwitch.icon = ContextCompat.getDrawable(getApplicationContext(),R.drawable.c);
                }
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)
        setSupportActionBar(toolbar)

        model = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(SearchViewViewModel::class.java)

        adapter =
            SearchResultAdapter{ entry: Entry ->
                entryClicked(entry)
            }

        searchResultList.layoutManager = LinearLayoutManager(this)
        searchResultList.adapter = adapter

        model.searchResults.observe(this, Observer {
            searchResults = it
            updateSearchResults(true)
        })

        model.searchHistory.observe(this, Observer {
            searchHistory = it
            updateSearchResults(true)
        })

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText!!.isNotBlank()) {
                    model.searchFor(newText
                                    .replace("ü", "u:")
                                    .replace("v", "u:"))
                } else {
                    searchResults = listOf()
                    updateSearchResults(true)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionary_search_view.clearFocus()
                return true
            }
        })

        dictionary_search_view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        updateSearchResults(false)
    }

    private fun updateSearchResults(scrollToStart: Boolean) {

        when (toolbar.dictionary_search_view.query.isBlank()) {
            true -> {
                resultCount.text = when (searchHistory.size) {
                    0 -> ""
                    else -> "Recently viewed: " + searchHistory.size.toString()
                }
                adapter.setEntries(searchHistory)
            }
            false -> {
                resultCount.text = when (searchResults.size) {
                    0 -> "Search results: 0"
                    in 1..999 -> "Search results: " + searchResults.size.toString()
                    else -> "Search results: 999+"
                }
                adapter.setEntries(searchResults)
            }
        }

        searchResultList.scrollToPosition(0)
    }

    private fun entryClicked(entry: Entry){
        model.addToSearchHistory(entry.id.toString())

        val intent = Intent(this, WordViewActivity::class.java)
        intent.putExtra("entry", entry)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.modeSwitch -> {
                model.toggleLanguage()
                var query = toolbar.dictionary_search_view.query
                toolbar.dictionary_search_view.setQuery("", false)
                toolbar.dictionary_search_view.setQuery(query, false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

