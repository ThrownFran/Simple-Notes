package brillembourg.notes.simple.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.ui.extras.setBackgroundDrawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
    }

    private fun setupToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(findViewById(R.id.toolbar))
        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar?.setBackgroundDrawable(R.drawable.blue_creyon_2)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp()
                || super.onSupportNavigateUp()
    }

    private fun navigateUp() = navController.navigateUp(appBarConfiguration)
}