package brillembourg.notes.simple.presentation.base

import android.content.Intent
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
import brillembourg.notes.simple.data.database.RoomBackupBuilder
import brillembourg.notes.simple.databinding.ActivityMainBinding
import brillembourg.notes.simple.domain.use_cases.notes.BackupModel
import brillembourg.notes.simple.presentation.custom_views.restartApp
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.custom_views.setBackgroundDrawable
import brillembourg.notes.simple.presentation.custom_views.showMessage
import brillembourg.notes.simple.presentation.custom_views.showToast
import brillembourg.notes.simple.presentation.ui_utils.asString
import brillembourg.notes.simple.presentation.ui_utils.contentViews
import brillembourg.notes.simple.util.UiText
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    val binding: ActivityMainBinding by contentViews(R.layout.activity_main)
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var roomBackupBuilder: RoomBackupBuilder

    lateinit var backupModel: BackupModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        binding.viewmodel = viewModel
        setupToolbar()
        renderStates()
        backupModel = roomBackupBuilder.prepareBackupInLocalStorage()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentReceiver(intent)
    }

    private fun renderStates() {
        safeUiLaunch {
            viewModel.mainUiState.collect { uiState ->
                contextMessageState(uiState)
                needsRestartState(uiState)
                userMessageState(uiState.userMessage)
            }
        }
    }

    private fun handleIntentReceiver(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND, Intent.ACTION_VIEW -> {
                if ("text/plain" == intent.type) {
                    onIncomingContentIntent(intent) // Handle text being sent
                }
            }
        }
    }

    private fun onIncomingContentIntent(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            viewModel.onIncomingContentFromExternalApp(it)
        }
    }


    private fun userMessageState(uiText: UiText?) {
        if (uiText == null) return

        val text = uiText.asString(this@MainActivity)
        showMessage(text) {
            viewModel.onUserMessageShown(uiText)
        }
    }

    private fun contextMessageState(uiState: MainUiState) {
        if (uiState.userToastMessage != null) {
            showToast(uiState.userToastMessage)
            viewModel.onToastMessageShown()
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
            setOf(R.id.homeFragment, R.id.categoriesFragment, R.id.trashFragment),
            binding.drawerLayout
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

                R.id.categoriesFragment -> {
                    navigateToCategories()
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
                    navigateToSettings()
                    closeDrawer()
                    true
                }
//                R.id.menu_privacy -> {
//                    true
//                }
                else -> false
            }
        }
    }


    private fun setupNavControllerListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.homeFragment) {
                binding.homeFab.show()
            } else {
                binding.homeFab.hide()
            }
        }
    }

    private fun restoreNotes() {
        viewModel.onRestoreNotes(backupModel)
    }

    private fun navigateToTrash() {
        if (navController.currentDestination?.id == R.id.trashFragment) return
        navController.navigate(R.id.trashFragment)
    }

    private fun navigateToHome() {
        if (navController.currentDestination?.id == R.id.homeFragment) return
        navController.navigate(R.id.homeFragment)
    }

    private fun navigateToCategories() {
        if (navController.currentDestination?.id == R.id.categoriesFragment) return
        navController.navigate(R.id.categoriesFragment)
    }

    private fun navigateToSettings() {
        if (navController.currentDestination?.id == R.id.settingsFragment) return
        navController.navigate(R.id.settingsFragment)
    }

    private fun backupNotes() {
        viewModel.onBackupNotes(backupModel)
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