package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_base_settings.view.*

class AppearanceSettingsFragment: SettingsBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.settingsToolbar.title = getString(R.string.appearance_settings_title)

        view.settingsToolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_theme)
        addPreferencesFromResource(R.xml.settings_appearance)
    }
}
