package com.mrpepe.pengyou.dictionary.searchView

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.activity_search_view.*
import kotlinx.android.synthetic.main.activity_search_view.view.*


class SearchViewActivity : BaseActivity() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var searchViewViewModel: SearchViewViewModel
    private lateinit var searchViewFragmentViewModel: SearchViewFragmentViewModel
    private var searchViewHadFocus = false
    private var pagePosition = 0
    private var keyboardVisible = false

    private var blockKeyboard = false

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.that_menu, menu)
        modeSwitch = menu?.getItem(0)!!

        searchViewViewModel.requestedLanguage.observe(this, Observer { language ->
            searchViewFragmentViewModel.requestedLanguage = language
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
        })

        searchViewFragmentViewModel.displayedLanguage.observe(this, Observer { language ->
            when (language) {
                SearchViewViewModel.SearchLanguage.ENGLISH -> {
                    modeSwitch.icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.e);
                }
                SearchViewViewModel.SearchLanguage.CHINESE -> {
                    modeSwitch.icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.c);
                }
                else -> {}
            }
        })

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_view)
        setSupportActionBar(toolbar)

        val sectionsPagerAdapter = SearchViewPagerAdapter(this, supportFragmentManager)
        val viewPager: CustomViewPager = findViewById(R.id.search_view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        // Deactivate horizontal paging
        viewPager.togglePagingEnabled()

        // Set icons for the tabs
        for (iTab in 0 until tabs.tabCount) {
            val tab = LayoutInflater.from(this).inflate(R.layout.tab_item, null)
            tab.findViewById<ImageView>(R.id.tab_icon).setImageResource(sectionsPagerAdapter.tabIcons[iTab])
            tabs.getTabAt(iTab)?.customView = tab
        }

        // Detect whether the keyboard is visible or not
        setupKeyboardVisibleListener()

        searchViewFragmentViewModel = ViewModelProvider(this)[SearchViewFragmentViewModel::class.java]
        searchViewViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                            .create(SearchViewViewModel::class.java)

        searchViewViewModel.englishSearchResults.observe(this, Observer {
            searchViewFragmentViewModel.englishSearchResults.value = it
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
        })

        searchViewViewModel.chineseSearchResults.observe(this, Observer {
            searchViewFragmentViewModel.chineseSearchResults.value = it
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
        })

        searchViewViewModel.searchHistory.observe(this, Observer {
            searchViewFragmentViewModel.searchHistory.value = it
            searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
        })

        searchViewFragmentViewModel.addCharacterToQuery.observe(this, Observer {
            val previousQuery = dictionary_search_view.query
            dictionary_search_view.setQuery("$previousQuery$it", false)
        })

        searchViewFragmentViewModel.deleteLastCharacterOfQuery.observe(this, Observer {
            dictionary_search_view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        })

        searchViewFragmentViewModel.submitFromDrawBoard.observe(this, Observer {
            blockKeyboard = true
            tabs.setScrollPosition(0, 0.toFloat(), true)
            viewPager.setCurrentItem(0)
        })

        searchViewFragmentViewModel.newHistoryEntry.observe(this, Observer { id ->
            searchViewViewModel.addToSearchHistory(id)
        })

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                searchViewFragmentViewModel.searchQuery = newText!!

                if (newText.isNotBlank()) {
                    searchViewViewModel.searchFor(newText
                                    .replace("ü", "u:")
                                    .replace("v", "u:"))
                } else {
                    searchViewFragmentViewModel.englishSearchResults.value = listOf()
                    searchViewFragmentViewModel.chineseSearchResults.value = listOf()
                    searchViewFragmentViewModel.updateSearchResults.value = UpdateSearchResultsMode.SNAPTOTOP
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionary_search_view.clearFocus()
                return true
            }
        })

        dictionary_search_view.setOnQueryTextFocusChangeListener( object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                when (hasFocus) {
                    true -> tabs.visibility = View.VISIBLE
                    false -> tabs.visibility = View.INVISIBLE
                }
            }
        })

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    // Keep keyboard hidden if desired or open it
                    if (blockKeyboard) {
                        blockKeyboard = false
                    }
                    else {
                        if (!keyboardVisible) {
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        }
                    }
                }
                else if (position == 1) {
                    hideKeyboard()
                }

                pagePosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
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
                if (searchViewFragmentViewModel.displayedLanguage.value != searchViewViewModel.requestedLanguage.value) {
                    val message = when (searchViewViewModel.requestedLanguage.value) {
                        SearchViewViewModel.SearchLanguage.CHINESE -> "No results for Chinese search available."
                        SearchViewViewModel.SearchLanguage.ENGLISH -> "No results for English search available."
                        else -> ""
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                else {
                    searchViewViewModel.toggleDisplayedLanguage()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupKeyboardVisibleListener() {
        val rootLayout = findViewById<View>(R.id.search_view_root)
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rec)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rec.bottom

            keyboardVisible = (keypadHeight > screenHeight*0.15)
        }
    }
}

