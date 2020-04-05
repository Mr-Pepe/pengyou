package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.View
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_base_settings.view.*

class TopLevelSettingsFragment: SettingsBaseFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_top_level, rootKey)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.settingsToolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }
}
