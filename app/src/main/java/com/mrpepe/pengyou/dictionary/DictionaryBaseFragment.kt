package com.mrpepe.pengyou.dictionary

import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.R

open class DictionaryBaseFragment : Fragment() {
    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)?.menu?.findItem(R.id.dictionarySearchFragment)?.setChecked(true)
    }
}
