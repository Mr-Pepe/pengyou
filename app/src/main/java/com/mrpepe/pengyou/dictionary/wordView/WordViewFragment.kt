package com.mrpepe.pengyou.dictionary.wordView

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.*
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.DictionaryBaseFragment

import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.fragment_word_view.*

private const val ARG_ENTRY = "entry"

class WordViewFragment : DictionaryBaseFragment(),
    StrokeOrderFragment.ToggleHorizontalPagingListener {

    private lateinit var viewPager: CustomViewPager

    private lateinit var wordViewViewModel: WordViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wordViewViewModel = ViewModelProvider(this).get(WordViewViewModel::class.java)

        arguments?.let { arguments ->
            val entry = arguments.get(ARG_ENTRY) as Entry

            wordViewViewModel.entry.value = entry
            SearchHistory.addToHistory(entry.id.toString())
            wordViewViewModel.init(entry)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_word_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionsPagerAdapter = WordViewPagerAdapter(childFragmentManager)
        viewPager = wordViewViewPager
        viewPager.adapter = sectionsPagerAdapter
        wordViewTabs.setupWithViewPager(viewPager)

        // Setup tab icons
        for (iTab in 0 until wordViewTabs.tabCount) {
            val tab = LayoutInflater.from(activity).inflate(R.layout.tab_item, null)
            tab.findViewById<ImageView>(R.id.tab_icon).setImageResource(sectionsPagerAdapter.tabIcons[iTab])
            wordViewTabs.getTabAt(iTab)?.customView = tab
        }

        wordViewToolbar.inflateMenu(R.menu.word_view_menu)

        wordViewToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.copyToClipboard -> {
                        var indicator = "(${getString(R.string.chinese_mode_simplified)})"
                        val headword = when (MainApplication.chineseMode) {
                            in listOf(ChineseMode.simplified, ChineseMode.simplifiedTraditional) -> {
                                wordViewViewModel.entry.value?.simplified
                            }
                            in listOf(ChineseMode.traditional, ChineseMode.traditionalSimplified) -> {
                                indicator = "(${getString(R.string.chinese_mode_traditional)})"
                                wordViewViewModel.entry.value?.traditional
                            }
                            else -> ""
                        }

                        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Headword", headword)

                        clipboard.setPrimaryClip(clip)

                        if (MainApplication.chineseMode in listOf(ChineseMode.simplifiedTraditional, ChineseMode.traditionalSimplified)) {
                            Toast.makeText(
                                activity,
                                "Copied $headword $indicator to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            Toast.makeText(
                                activity,
                                "Copied $headword to clipboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        true
                    }
                    else -> true
                }
            }
        })

        wordViewViewModel.entry.observe(viewLifecycleOwner, Observer { entry ->
            setHeadwordPinyinAndHsk(entry)
        })
    }

    override fun onResume() {
        super.onResume()
        wordViewViewModel.entry.value?.let { setHeadwordPinyinAndHsk(it) }
    }

    fun setHeadwordPinyinAndHsk(entry: Entry) {
        wordViewHeadword.text = HeadwordFormatter().format(entry, MainApplication.chineseMode)
        wordViewPinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, MainApplication.pinyinMode)

        when (entry.hsk) {
            7 -> {
                wordViewContainer.removeView(wordViewHsk)
            }
            else -> {
                if (wordViewHsk.parent == null) {
                    wordViewContainer.addView(wordViewHsk)
                }
                wordViewHsk.text = "HSK " + entry.hsk.toString()
            }
        }
    }

    override fun toggleHorizontalPaging() {
        viewPager.togglePagingEnabled()
    }

    fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(entry: Entry) =
            WordViewFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ENTRY, entry)
                }
            }
    }
}
