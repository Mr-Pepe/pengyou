package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R

open class SettingsBaseFragment: PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)?.menu?.findItem(R.id.topLevelSettingsFragment)?.isChecked =
            true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) { }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<MultiSelectListPreference>("stroke_background")?.let {preference ->
            preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _,
                                                                                       newValue ->
                preference.setSummaryFromValues(newValue as Set<String>)
                true
            }

            preference.setSummaryFromValues(preference.values)
        }
    }
}

fun MultiSelectListPreference.setSummaryFromValues(values: Set<String>) {

    summary = if (values.isNotEmpty()) {
        values.joinToString(", ") { entries[findIndexOfValue(it)] }
    }
    else {
        MainApplication.getContext().resources.getString(R.string.none)
    }
}
