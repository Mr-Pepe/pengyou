package com.mrpepe.pengyou

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.dictionary.search.DictionarySearchFragment
import com.mrpepe.pengyou.settings.SettingsFragment

class HomeActivity : BaseActivity(),
    DictionarySearchFragment.DictionarySearchFragmentInteractionListener,
    SettingsFragment.SettingsFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_container) as NavHostFragment

        val navController = host.navController

        AppBarConfiguration(navController.graph)

        setupBottomNavMenu(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                Integer.toString(destination.id)
            }

            Toast.makeText(this@HomeActivity, "Navigated to $dest", Toast.LENGTH_SHORT).show()
            Log.d("HomeActivity", "Navigated to $dest")
        }

    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav?.setupWithNavController(navController)

        bottomNav?.setOnNavigationItemSelectedListener { item ->
            when (item.title) {
                getString(R.string.bottom_navigation_view_dictionary) -> {
                    findNavController(R.id.main_container).navigate(R.id.dictionarySearchFragment)
                }
                getString(R.string.bottom_navigation_view_settings) -> {
                    findNavController(R.id.main_container).navigate(R.id.settingsFragment)
                }
                else -> {}
            }

            true
        }
    }

}
