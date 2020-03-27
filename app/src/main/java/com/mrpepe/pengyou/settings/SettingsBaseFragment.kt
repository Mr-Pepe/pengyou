package com.mrpepe.pengyou.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.R

open class SettingsBaseFragment: PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)?.menu?.findItem(R.id.topLevelSettingsFragment)?.isChecked =
            true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { }
}
