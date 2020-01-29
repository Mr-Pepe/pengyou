package com.mrpepe.pengyou.dictionary.searchView

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mrpepe.pengyou.BaseActivity
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.UpdateSearchResultsMode
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.wordView.WordViewActivity
import com.mrpepe.pengyou.dictionary.wordView.WordViewFragmentViewModel
import com.mrpepe.pengyou.dictionary.wordView.WordViewPagerAdapter

import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.activity_search_view.view.*
import kotlinx.android.synthetic.main.fragment_search_result_list.*

class SearchViewActivity : BaseActivity() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var searchViewViewModel: SearchViewViewModel
    private lateinit var searchViewFragmentViewModel: SearchViewFragmentViewModel
    private var searchViewHadFocus = false

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.that_menu, menu)
        modeSwitch = menu?.getItem(0)!!

        searchViewViewModel.searchLanguage.observe(this, Observer { mode ->
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

        val sectionsPagerAdapter = SearchViewPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.search_view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        searchViewFragmentViewModel = ViewModelProviders.of(this)[SearchViewFragmentViewModel::class.java]
        searchViewViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(SearchViewViewModel::class.java)

        searchViewViewModel.searchResults.observe(this, Observer {
            searchViewFragmentViewModel.searchResults.value = it
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP

        })

        searchViewViewModel.searchHistory.observe(this, Observer {
            searchViewFragmentViewModel.searchHistory.value = it
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
        })

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                searchViewFragmentViewModel.searchQuery = newText!!

                if (newText!!.isNotBlank()) {
                    searchViewViewModel.searchFor(newText
                                    .replace("ü", "u:")
                                    .replace("v", "u:"))
                } else {
                    searchViewFragmentViewModel.searchResults.value = listOf()
                    searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionary_search_view.clearFocus()
                return true
            }
        })

        searchViewFragmentViewModel.newHistoryEntry.observe(this, Observer { id ->
            searchViewViewModel.addToSearchHistory(id)
        })

        dictionary_search_view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.DONTSNAPTOTOP

        if (searchViewHadFocus) {
            dictionary_search_view.requestFocus()
            searchViewHadFocus = false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.modeSwitch -> {
                searchViewViewModel.toggleLanguage()
                var query = toolbar.dictionary_search_view.query
                toolbar.dictionary_search_view.setQuery("", false)
                toolbar.dictionary_search_view.setQuery(query, false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

