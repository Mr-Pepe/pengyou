package com.mrpepe.pengyou.dictionary.singleEntryView

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.activity_word_view.*

class WordViewActivity : BaseActivity(), StrokeOrderFragment.ToggleHorizontalPaging {
    lateinit var wordViewViewModel : WordViewViewModel
    lateinit var wordViewFragmentViewModel : WordViewFragmentViewModel

    lateinit var viewPager: CustomViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_view)
        setSupportActionBar(word_view_toolbar)

        val sectionsPagerAdapter = WordViewPagerAdapter(this, supportFragmentManager)
        viewPager = findViewById(R.id.word_view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.dictionarySearchInputMethodTabs)
        tabs.setupWithViewPager(viewPager)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup tab icons
        for (iTab in 0 until tabs.tabCount) {
            val tab = LayoutInflater.from(this).inflate(R.layout.tab_item, null)
            tab.findViewById<ImageView>(R.id.tab_icon).setImageResource(sectionsPagerAdapter.tabIcons[iTab])
            tabs.getTabAt(iTab)?.customView = tab
        }

        wordViewFragmentViewModel = ViewModelProvider(this)[WordViewFragmentViewModel::class.java]
        wordViewViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                    .create(WordViewViewModel::class.java)

        if (!wordViewViewModel.isInitialized) {
            wordViewViewModel.init(intent.extras?.get("entry") as Entry)
        }

        wordViewViewModel.entry.observe(this, Observer { entry ->
            headword.text = HeadwordFormatter().format(entry, ChineseMode.SIMPLIFIED)
            pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, PinyinMode.MARKS)
            
            wordViewFragmentViewModel.entry.value = entry
        })

        wordViewViewModel.wordsContaining.observe(this, Observer { wordsContaining ->
            wordViewFragmentViewModel.wordsContaining.value = wordsContaining
        })

        wordViewViewModel.decompositions.observe(this, Observer { decompositions ->
            wordViewFragmentViewModel.decompositions.value = decompositions
        })

        wordViewViewModel.strokeOrders.observe(this, Observer { strokeOrders ->
            wordViewFragmentViewModel.strokeOrders.value = strokeOrders
        })
    }

    override fun toggleHorizontalPaging() {
        viewPager.togglePagingEnabled()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.copyToClipboard -> {
                val headword = wordViewViewModel.entry.value?.simplified
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Headword", headword)

                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Copied $headword to clipboard", Toast.LENGTH_SHORT).show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
