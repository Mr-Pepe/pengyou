package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.mrpepe.pengyou.R
import com.rarepebble.colorpicker.ColorPreference
import kotlinx.android.synthetic.main.fragment_base_settings.view.*

class HeadwordColoringSettingsFragment: SettingsBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.settingsToolbar.title = getString(R.string.headword_coloring)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.headword_coloring_settings, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (preference is ColorPreference) {
            preference.showDialog(this, 0)
        }
        else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
