package com.mrpepe.pengyou.dictionary.search

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.mrpepe.pengyou.*

import kotlinx.android.synthetic.main.fragment_dictionary_search.*
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchInputMethodTabs
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchSearchBox
import kotlinx.android.synthetic.main.fragment_dictionary_search.dictionarySearchViewPager

class DictionarySearchFragment : Fragment() {
    private lateinit var modeSwitch: MenuItem
    private lateinit var dictionaryViewModel: DictionarySearchViewModel
    private var keyboardVisible = false
    private var blockKeyboard = false

    private var listener: DictionarySearchFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            dictionaryViewModel = ViewModelProvider(it).get(DictionarySearchViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dictionary_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dictionarySearchToolbar.inflateMenu(R.menu.dictionary_search_menu)
        modeSwitch = dictionarySearchToolbar.menu.getItem(0)

        dictionaryViewModel.displayedLanguage.observe(viewLifecycleOwner, Observer { language ->
            when (language) {
                DictionarySearchViewModel.SearchLanguage.ENGLISH -> {
                    modeSwitch.icon = ContextCompat.getDrawable(MainApplication.getContext(), R.drawable.ic_english_mode);
                }
                DictionarySearchViewModel.SearchLanguage.CHINESE -> {
                    modeSwitch.icon = ContextCompat.getDrawable(MainApplication.getContext(), R.drawable.ic_chinese_mode);
                }
                else -> {}
            }
        })

        val sectionsPagerAdapter = DictionarySearchPagerAdapter(childFragmentManager)
        val viewPager : CustomViewPager = dictionarySearchViewPager
        viewPager.adapter = sectionsPagerAdapter
        dictionarySearchInputMethodTabs.setupWithViewPager(viewPager)
        // Deactivate horizontal paging
        viewPager.togglePagingEnabled()

        // Set icons for the tabs
        for (iTab in 0 until dictionarySearchInputMethodTabs.tabCount) {
            val tab = LayoutInflater.from(activity).inflate(R.layout.tab_item, null)
            tab.findViewById<ImageView>(R.id.tab_icon).setImageResource(sectionsPagerAdapter.tabIcons[iTab])
            dictionarySearchInputMethodTabs.getTabAt(iTab)?.customView = tab
        }

        // Detect whether the keyboard is visible or not
        setupKeyboardVisibleListener(view)

        dictionarySearchSearchBox.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != dictionaryViewModel.searchQuery) {
                    dictionaryViewModel.searchQuery = newText!!
                    dictionaryViewModel.search()
                }

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionarySearchSearchBox.clearFocus()
                return true
            }
        })

        dictionarySearchSearchBox.setOnQueryTextFocusChangeListener( object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                when (hasFocus) {
                    true -> if (dictionarySearchInputMethodTabs.parent == null) {
                        dictionarySearchLinearLayout.addView(dictionarySearchInputMethodTabs, 1)
                    }
                    false -> dictionarySearchLinearLayout.removeView(dictionarySearchInputMethodTabs)
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
                                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        }
                        dictionarySearchSearchBox.requestFocus()
                    }
                }
                else if (position == 1) {
                    dictionarySearchSearchBox.requestFocus()
                    hideKeyboard()
                }
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

        dictionarySearchToolbar.setOnMenuItemClickListener(object: Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.modeSwitch -> {
                        if (dictionaryViewModel.displayedLanguage.value != dictionaryViewModel.requestedLanguage.value &&
                                dictionarySearchSearchBox.query.isNotBlank()) {
                            val message = when (dictionaryViewModel.requestedLanguage.value) {
                                DictionarySearchViewModel.SearchLanguage.CHINESE -> "No results for Chinese search available."
                                DictionarySearchViewModel.SearchLanguage.ENGLISH -> "No results for English search available."
                                else -> ""
                            }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        }
                        else {
                            dictionaryViewModel.toggleDisplayedLanguage()
                        }

                        true
                    }
                        else -> true
                }
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DictionarySearchFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun setupKeyboardVisibleListener(rootLayout: View) {
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rec)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rec.bottom

            keyboardVisible = (keypadHeight > screenHeight*0.15)
        }
    }

    fun addCharacterToQuery(newChar: String) {
        val previousQuery = dictionarySearchSearchBox.query
        dictionarySearchSearchBox.setQuery("$previousQuery$newChar", false)
    }

    fun deleteLastCharacterOfQuery() {
        dictionarySearchSearchBox.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }

    fun submitQuery() {
        blockKeyboard = true
        dictionarySearchViewPager.requestFocus()
        dictionarySearchInputMethodTabs.setScrollPosition(0, 0.toFloat(), true)
        dictionarySearchViewPager.setCurrentItem(0)
    }


    // TODO: Rename method, update argument and hook method into UI event
//    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface DictionarySearchFragmentInteractionListener {
        // TODO: Update argument type and name
//        fun onSearchViewInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DictionarySearchFragment().apply {}
    }
}
