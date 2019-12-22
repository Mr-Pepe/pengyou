package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.activity_word_view.*

class WordViewActivity : AppCompatActivity() {
    lateinit var wordViewViewModel : WordViewViewModel
    lateinit var wordViewFramentViewModel : WordViewFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_view)
        val sectionsPagerAdapter = WordViewPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        wordViewFramentViewModel = ViewModelProviders.of(this)[WordViewFragmentViewModel::class.java]
        wordViewViewModel = ViewModelProvider.AndroidViewModelFactory(application)
                                    .create(WordViewViewModel::class.java)

        if (!wordViewViewModel.isInitialized) {
            wordViewViewModel.init(intent.extras?.get("entry") as Entry)
        }

        wordViewViewModel.entry.observe(this, Observer { entry ->
            headword.text = entry.simplified
            pinyin.text = entry.pinyin
            wordViewFramentViewModel.entry.value = entry
        })

        wordViewViewModel.wordsContaining.observe(this, Observer { wordsContaining ->
            wordViewFramentViewModel.wordsContaining.value = wordsContaining
        })
    }
}
