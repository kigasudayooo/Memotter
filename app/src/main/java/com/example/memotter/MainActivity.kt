package com.example.memotter

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.memotter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fabNewMemo.setOnClickListener { view ->
            // Navigate to new memo fragment
            navController.navigate(R.id.nav_new_memo)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_timeline,
                R.id.nav_new_memo,
                R.id.nav_search,
                R.id.nav_favorites,
                R.id.nav_templates,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set up navigation item selected listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_timeline -> {
                    navController.navigate(R.id.nav_timeline)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_new_memo -> {
                    navController.navigate(R.id.nav_new_memo)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_search -> {
                    navController.navigate(R.id.nav_search)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_favorites -> {
                    navController.navigate(R.id.nav_favorites)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_templates -> {
                    navController.navigate(R.id.nav_templates)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    navController.navigate(R.id.nav_settings)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}