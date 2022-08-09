package brillembourg.notes.simple.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.ActivityMainBinding
import brillembourg.notes.simple.ui.extras.setBackgroundDrawable
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
    }

    private fun setupToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController
//        appBarConfiguration = AppBarConfiguration.Builder(
//            R.id.homeFragment,
//            R.id.trashFragment,
//        )
//            .setDrawerLayout(binding.drawerLayout)
//            .build()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.trashFragment
            ), binding.drawerLayout
        )

//        appBarConfiguration = AppBarConfiguration(
//            navGraph = navController.graph,
//            drawerLayout = binding.drawerLayout
//        )
        setSupportActionBar(binding.toolbar)

        setupActionBarWithNavController(
            navController = navController,
            configuration = appBarConfiguration
        )

        binding.navView.setupWithNavController(navController)
        supportActionBar?.setBackgroundDrawable(R.drawable.blue_creyon_2)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp()
                || super.onSupportNavigateUp()
    }

    private fun navigateUp() = navController.navigateUp(appBarConfiguration)
}