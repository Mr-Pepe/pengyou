package com.mrpepe.pengyou

import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var dictionarySearchViewModel: DictionarySearchViewModel

    private var listener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "pinyin_notation" -> {
                    MainApplication.pinyinMode = sharedPreferences?.getString(key, PinyinMode.marks) ?: PinyinMode.marks
                }
                "chinese_mode" -> {
                    MainApplication.chineseMode = sharedPreferences?.getString(key, ChineseMode.simplified) ?: ChineseMode.simplified
                }
                "theme" -> {
                    setTheme()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        setTheme()
        super.onCreate(savedInstanceState)
        MainApplication.homeActivity = this
        setContentView(R.layout.activity_home)
        setupKeyboardVisibleListener(findViewById(R.id.homeActivity))

        dictionarySearchViewModel = ViewModelProvider(this)[DictionarySearchViewModel::class.java]

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainContainer) as NavHostFragment

        val navController = host.navController

        AppBarConfiguration(navController.graph)

        setupBottomNavMenu(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                destination.id.toString()
            }

//            Toast.makeText(this@HomeActivity, "Navigated to $dest", Toast.LENGTH_SHORT).show()
            Log.d("HomeActivity", "Navigated to $dest")
        }

        MainApplication.pinyinMode =
            PreferenceManager
                .getDefaultSharedPreferences(MainApplication.getContext())
                .getString("pinyin_notation", PinyinMode.marks) ?: PinyinMode.marks

        MainApplication.chineseMode =
            PreferenceManager
                .getDefaultSharedPreferences(MainApplication.getContext())
                .getString("chinese_mode", ChineseMode.simplified) ?: ChineseMode.simplified

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun isNightMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
    }

    private fun setTheme() {
        val mode = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext())?.getString("theme", "") ?: ""

        when (mode) {
            getString(R.string.theme_light) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            getString(R.string.theme_dark) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            getString(R.string.theme_battery_saver) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            )
            getString(R.string.theme_system_default) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            else -> {}
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)
        bottomNav?.setupWithNavController(navController)

        bottomNav?.setOnNavigationItemSelectedListener { item ->
            when (item.title) {
                getString(R.string.bottom_navigation_view_dictionary) -> {
                    dictionarySearchViewModel.searchQuery.value = ""
                    findNavController(R.id.mainContainer).navigate(R.id.globalOpenDictionaryAction)
                }
                getString(R.string.bottom_navigation_view_settings) -> {
                    findNavController(R.id.mainContainer).navigate(R.id.topLevelSettingsFragment)
                }
                else -> {}
            }

            true
        }
    }

    private fun setupKeyboardVisibleListener(rootLayout: View) {
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rec = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rec)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rec.bottom

            MainApplication.keyboardVisible = (keypadHeight > screenHeight*0.15)

            when (MainApplication.keyboardVisible) {
                false -> if (bottomNavigationMenu.parent == null) {
                    homeActivityLinearLayout.addView(bottomNavigationMenu, 1)
                }
                true -> homeActivityLinearLayout.removeView(bottomNavigationMenu)
            }
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat?,
        pref: Preference?
    ): Boolean {

        when (pref?.key) {
            "appearance" -> findNavController(R.id.mainContainer).navigate(R.id.actionTopLevelSettingsToAppearanceSettings)
            "headword_coloring" -> findNavController(R.id.mainContainer).navigate(R.id.actionAppearanceSettingsToHeadwordColoringSettings)
            else -> {}
        }
        
        return true
    }
}
