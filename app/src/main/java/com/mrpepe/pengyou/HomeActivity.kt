package com.mrpepe.pengyou

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrpepe.pengyou.dictionary.search.DictionarySearchViewModel
import com.mrpepe.pengyou.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_dictionary_search.*

class HomeActivity : BaseActivity(),
    SettingsFragment.SettingsFragmentInteractionListener {

    private lateinit var dictionarySearchViewModel: DictionarySearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupKeyboardVisibleListener(findViewById(R.id.homeActivity))

        dictionarySearchViewModel = ViewModelProvider(this)[DictionarySearchViewModel::class.java]

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

//            Toast.makeText(this@HomeActivity, "Navigated to $dest", Toast.LENGTH_SHORT).show()
            Log.d("HomeActivity", "Navigated to $dest")
        }

    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationMenu)
        bottomNav?.setupWithNavController(navController)

        bottomNav?.setOnNavigationItemSelectedListener { item ->
            when (item.title) {
                getString(R.string.bottom_navigation_view_dictionary) -> {
                    dictionarySearchViewModel.searchQuery = ""
                    findNavController(R.id.main_container).navigate(R.id.globalOpenDictionaryAction)
                }
                getString(R.string.bottom_navigation_view_settings) -> {
                    findNavController(R.id.main_container).navigate(R.id.settingsFragment)
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

}
