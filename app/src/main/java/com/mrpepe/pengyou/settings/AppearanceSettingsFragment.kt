package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_base_settings.view.*

class AppearanceSettingsFragment: SettingsBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.settingsToolbar.title = getString(R.string.appearance_settings_title)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.theme_settings)
        addPreferencesFromResource(R.xml.appearance_settings)
    }
}
