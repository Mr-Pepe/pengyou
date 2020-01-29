package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.activity_word_view.*

class WordViewActivity : BaseActivity() {
    lateinit var wordViewViewModel : WordViewViewModel
    lateinit var wordViewFragmentViewModel : WordViewFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_view)
        val sectionsPagerAdapter = WordViewPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.word_view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        wordViewFragmentViewModel = ViewModelProviders.of(this)[WordViewFragmentViewModel::class.java]
        wordViewViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                    .create(WordViewViewModel::class.java)

        if (!wordViewViewModel.isInitialized) {
            wordViewViewModel.init(intent.extras?.get("entry") as Entry)
        }

        wordViewViewModel.entry.observe(this, Observer { entry ->
            headword.text = HeadwordFormatter().format(entry, ChineseMode.BOTH)
            pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, PinyinMode.MARKS)

            hsk.text = when (entry.hsk) {
                7 -> ""
                else -> "HSK " + entry.hsk.toString()
            }
            
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
}
