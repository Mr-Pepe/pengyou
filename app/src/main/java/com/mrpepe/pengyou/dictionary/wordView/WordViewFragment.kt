package com.mrpepe.pengyou.dictionary.wordView

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.*

import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel
import kotlinx.android.synthetic.main.fragment_dictionary_search.*
import kotlinx.android.synthetic.main.fragment_word_view.*

private const val ARG_ENTRY = "entry"

class WordViewFragment : Fragment(),
    StrokeOrderFragment.ToggleHorizontalPagingListener {

    private lateinit var entry: Entry
    private lateinit var viewPager: CustomViewPager

    private lateinit var wordViewViewModel: WordViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entry = it.get(ARG_ENTRY) as Entry
        }

        activity?.let {
            wordViewViewModel = ViewModelProvider(it).get(WordViewViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
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

        if (!wordViewViewModel.isInitialized || entry != wordViewViewModel.entry.value) {
            wordViewViewModel.init(entry)
        }

        wordViewViewModel.entry.observe(viewLifecycleOwner, Observer { entry ->
            wordViewHeadword.text = HeadwordFormatter().format(entry, ChineseMode.SIMPLIFIED)
            wordViewPinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, PinyinMode.MARKS)
        })

        wordViewToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.copyToClipboard -> {
                        val headword = wordViewViewModel.entry.value?.simplified
                        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Headword", headword)

                        clipboard.setPrimaryClip(clip)

                        Toast.makeText(activity, "Copied $headword to clipboard", Toast.LENGTH_SHORT).show()

                        true
                    }
                    else -> true
                }
            }
        })
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