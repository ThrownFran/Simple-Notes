package brillembourg.notes.simple.presentation.base

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
import brillembourg.notes.simple.presentation.extras.*
import brillembourg.notes.simple.presentation.home.HomeFragmentDirections
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.presentation.ui_utils.contentViews
import brillembourg.notes.simple.util.asString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    val binding: ActivityMainBinding by contentViews(R.layout.activity_main)
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var messageManager: MessageManager

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
        safeUiLaunch {
            viewModel.mainUiState.collect { uiState ->
                contextMessageState(uiState)
                needsRestartState(uiState)
            }
        }

        safeUiLaunch {
            messageManager.message.collect { uiText ->

                if (uiText == null) return@collect

                val text = uiText.asString(this@MainActivity)
                showMessage(text) {
                    messageManager.setMessageShown(uiText)
                }
            }
        }
    }

    private fun contextMessageState(uiState: MainUiState) {
        if (uiState.userContextMessage != null) {
            showToast(uiState.userContextMessage)
            viewModel.onMessageShown()
        }
    }

    private fun needsRestartState(uiState: MainUiState) {
        if (uiState.needsRestartApp) {
            restartApp()
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
        supportActionBar?.setBackgroundDrawable(R.drawable.toolbar_shape)

        setupActionBarWithNavController(
            navController = navController,
            configuration = appBarConfiguration
        )

        binding.navView.setupWithNavController(navController)
        //TODO
        setupNavControllerListener()
        setupDrawerListener()
    }

    private fun setupDrawerListener() {
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

    private fun setupNavControllerListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.homeFab.apply {
                if (destination.id == R.id.homeFragment) show() else hide()
            }
        }
    }

    private fun restoreNotes() {
        viewModel.onRestoreNotes()
    }

    private fun navigateToTrash() {
        if (navController.currentDestination?.id == R.id.trashFragment) return
        navController.navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment())
//        navController.navigate(brillembourg.notes.simple.R.id.trashFragment,navBuilder.build())
    }

    private fun navigateToHome() {
        if (navController.currentDestination?.id == R.id.homeFragment) return
        navController.popBackStack()
    }

    private fun backupNotes() {
        viewModel.onBackupNotes()
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