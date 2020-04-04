package com.mrpepe.pengyou.dictionary.wordView

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.*
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

        val wordViewPagerAdapter = WordViewPagerAdapter(childFragmentManager)
        viewPager = wordViewViewPager
        viewPager.adapter = wordViewPagerAdapter
        wordViewTabs.setupWithViewPager(viewPager)

        viewPager.offscreenPageLimit = 1

        wordViewToolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        wordViewToolbar.inflateMenu(R.menu.word_view_menu)

        wordViewToolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.copyToClipboard -> {
                    wordViewViewModel.entry.value?.let { entry ->
                        activity?.let { activity ->
                            copyHeadwordToClipboard(activity, entry)
                        }
                    }

                    true
                }
                else -> true
            }
        }

        wordViewViewModel.entry.observe(viewLifecycleOwner, Observer { entry ->
            setHeadwordPinyinAndHsk(entry)
        })
    }

    override fun onResume() {
        super.onResume()
        wordViewViewModel.entry.value?.let { setHeadwordPinyinAndHsk(it) }
    }

    private fun setHeadwordPinyinAndHsk(entry: Entry) {
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
