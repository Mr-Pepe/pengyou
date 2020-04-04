package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.rarepebble.colorpicker.ColorPreference
import kotlinx.android.synthetic.main.fragment_base_settings.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HeadwordColoringSettingsFragment: SettingsBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.settingsToolbar.title = getString(R.string.headword_coloring)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_headword_coloring, rootKey)

        val resetToDefaultButton = findPreference<Preference>("headword_colors_to_default")
        resetToDefaultButton?.setOnPreferenceClickListener {
            MainScope().launch {
                with(PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext()).edit()) {
                    putInt("first_tone_color", MainApplication.getContext().getColor(R.color.tone1DefaultColor))
                    putInt("second_tone_color", MainApplication.getContext().getColor(R.color.tone2DefaultColor))
                    putInt("third_tone_color", MainApplication.getContext().getColor(R.color.tone3DefaultColor))
                    putInt("fourth_tone_color", MainApplication.getContext().getColor(R.color.tone4DefaultColor))
                    putInt("fifth_tone_color", MainApplication.getContext().getColor(R.color.tone5DefaultColor))
                    commit()
                }

                setPreferencesFromResource(R.xml.settings_headword_coloring, rootKey)
            }

            true
        }
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
