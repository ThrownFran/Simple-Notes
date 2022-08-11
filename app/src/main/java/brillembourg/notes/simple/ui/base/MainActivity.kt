package brillembourg.notes.simple.ui.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.ActivityMainBinding
import brillembourg.notes.simple.domain.ContextDomain
import brillembourg.notes.simple.ui.extras.setBackgroundDrawable
import brillembourg.notes.simple.ui.extras.showToast
import brillembourg.notes.simple.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupObservers()
        viewModel.prepareBackupNotes(ContextDomain(this))
    }

    private fun setupObservers() {
        viewModel.messageEvent.observe(this) {
            showToast(it)
        }
    }

    private fun setupToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController

        //top level configuration
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.homeFragment, R.id.trashFragment), binding.drawerLayout
//        )

        appBarConfiguration = AppBarConfiguration(
            navGraph = navController.graph,
            drawerLayout = binding.drawerLayout
        )
        setSupportActionBar(binding.toolbar)

        setupActionBarWithNavController(
            navController = navController,
            configuration = appBarConfiguration
        )

        binding.navView.setupWithNavController(navController)
        supportActionBar?.setBackgroundDrawable(R.drawable.blue_creyon_2)

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.trashFragment -> {
                    navController.navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment())
                    closeDrawer()
                    true
                }
                R.id.menu_backup -> {
                    viewModel.backupNotes()
                    closeDrawer()
                    true
                }
                R.id.menu_restore -> {
                    viewModel.restoreNotes()
                    closeDrawer()
                    true
                }
                R.id.menu_settings -> {
                    true
                }
                R.id.menu_privacy -> {
                    true
                }
                else -> false
            }
        }
    }

    public fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp()
                || super.onSupportNavigateUp()
    }

    private fun navigateUp() = navController.navigateUp(appBarConfiguration)
}