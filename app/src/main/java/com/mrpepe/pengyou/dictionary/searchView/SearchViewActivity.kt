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
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity

import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.activity_search_view.view.*
import kotlinx.android.synthetic.main.search_result_list.*

class SearchViewActivity : AppCompatActivity() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var model: SearchViewViewModel

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

        val adapter =
            SearchResultAdapter{ entry: Entry ->
                entryClicked(entry)
            }

        searchResultList.layoutManager = LinearLayoutManager(this)
        searchResultList.adapter = adapter

        model.searchResults.observe(this, Observer { searchResults ->
            resultCount.text = when (searchResults.size) {
                0 -> when(toolbar.dictionary_search_view.query.isBlank()) {
                    true -> ""
                    false -> "0 Results"
                }
                in 1..999 -> searchResults.size.toString() + " Results"
                else -> "999+ Results"
            }
            
            searchResults?.let { adapter.setEntries(searchResults) }
            searchResultList.scrollToPosition(0)
        })

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText!!.isNotBlank()) {
                    model.searchFor(newText
                                    .replace("ü", "u:")
                                    .replace("v", "u:"))
                } else {
                    adapter.setEntries(emptyList())
                    resultCount.text = ""
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

