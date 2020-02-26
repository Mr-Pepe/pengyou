package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_base_settings.view.*

open class SettingsBaseFragment: PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)?.menu?.findItem(R.id.topLevelSettingsFragment)?.setChecked(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { }
}
