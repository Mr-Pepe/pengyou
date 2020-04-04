package com.mrpepe.pengyou.settings

import android.os.Bundle
import com.mrpepe.pengyou.R

class TopLevelSettingsFragment: SettingsBaseFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_top_level, rootKey)
    }
}
