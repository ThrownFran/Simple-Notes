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
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    val binding: ActivityMainBinding by contentViews(R.layout.activity_main)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewmodel = viewModel
        setupToolbar()
        setupObservers()
        prepareBackup()
    }

    private fun prepareBackup() {
        viewModel.prepareBackupNotes(ContextDomain(this))
    }

    private fun setupObservers() {
        viewModel.messageEvent.observe(this) {
            showToast(it)
        }
    }

    private fun setupToolbar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        //top level configuration
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.trashFragment), binding.drawerLayout
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
        //TODO
        supportActionBar?.setBackgroundDrawable(R.drawable.toolbar_shape)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.homeFab.apply {
                if (destination.id == R.id.homeFragment) show() else hide()
            }
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    navigateToHome()
                    closeDrawer()
                    true
                }
                R.id.trashFragment -> {
                    navigateToTrash()
                    closeDrawer()
                    true
                }

                R.id.menu_backup -> {
                    backupNotes()
                    closeDrawer()
                    false
                }
                R.id.menu_restore -> {
                    restoreNotes()
                    closeDrawer()
                    false
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

    private fun restoreNotes() {
        viewModel.restoreNotes()
    }

    private fun navigateToTrash() {
        navController.navigate(R.id.trashFragment)
    }

    private fun navigateToHome() {
        navController.navigate(R.id.homeFragment)
    }

    private fun backupNotes() {
        viewModel.backupNotes()
    }

    private fun closeDrawer() {
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