package com.mrpepe.pengyou.dictionary

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.ui.wordView.WordViewPagerAdapter
import com.mrpepe.pengyou.dictionary.ui.wordView.WordViewViewModel
import kotlinx.android.synthetic.main.activity_word_view.*

class WordViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_view)
        val sectionsPagerAdapter = WordViewPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        var model: WordViewViewModel = ViewModelProviders.of(this).get(WordViewViewModel::class.java)

        model.entry = intent.extras?.get("entry") as Entry

        headword.text = model.entry.simplified
        pinyin.text = model.entry.pinyin
    }
}