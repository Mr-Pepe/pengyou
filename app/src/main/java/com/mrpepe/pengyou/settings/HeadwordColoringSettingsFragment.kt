package com.mrpepe.pengyou.settings

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.SearchHistory
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
        setPreferencesFromResource(R.xml.headword_coloring_settings, rootKey)

        val resetToDefaultButton = findPreference<Preference>("headword_colors_to_default")
        resetToDefaultButton?.setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener{
            override fun onPreferenceClick(preference: Preference?): Boolean {
                MainScope().launch {
                    with(PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext()).edit()) {
                        putInt("first_tone_color", MainApplication.getContext().getColor(R.color.tone1_default_color))
                        putInt("second_tone_color", MainApplication.getContext().getColor(R.color.tone2_default_color))
                        putInt("third_tone_color", MainApplication.getContext().getColor(R.color.tone3_default_color))
                        putInt("fourth_tone_color", MainApplication.getContext().getColor(R.color.tone4_default_color))
                        putInt("fifth_tone_color", MainApplication.getContext().getColor(R.color.tone5_default_color))
                        commit()
                    }

                    setPreferencesFromResource(R.xml.headword_coloring_settings, rootKey)
                }

                return true
            }
        })
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
