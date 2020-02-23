package com.mrpepe.pengyou.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.mrpepe.pengyou.R

class TopLevelSettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.top_level_settings, rootKey)
    }
}
