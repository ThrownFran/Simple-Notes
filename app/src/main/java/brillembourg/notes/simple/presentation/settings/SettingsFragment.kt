package brillembourg.forecast.weather.simple.presentation.settings

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentSettingsBinding
import brillembourg.notes.simple.domain.models.ThemeMode
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.settings.IsOption
import brillembourg.notes.simple.presentation.settings.SettingsState
import brillembourg.notes.simple.presentation.settings.SettingsViewModel
import brillembourg.notes.simple.presentation.ui_utils.MyLogger
import brillembourg.notes.simple.util.GenericException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
//        showToolbar()
        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.settingsContraintTheme.setOnClickListener {
            clickChooseTheme()
        }

        binding.settingsContraintAbout.setOnClickListener {
            clickNavigateToAbout()
        }

        binding.settingsContraintLicenses.setOnClickListener {
            clickNavigateToLicenses()
        }

        binding.settingsContraintReview.setOnClickListener {
            clickRateApp()
        }
    }

    private fun clickRateApp() {
        val manager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            } else {
                // There was some problem, log or handle the error code.
                task.exception?.let { MyLogger.record(it) }
                navigateToPlayStore()
            }
        }
    }

    private fun navigateToPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://play.google.com/store/apps/details?id=brillembourg.notes.simple"
                )
                setPackage("com.android.vending")
            }
            activity?.startActivity(intent)
        } catch (e: Exception) {
            val exception = GenericException("Error rating App")
            MyLogger.record(exception)
        }
    }

    private fun clickNavigateToAbout() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAboutFragment())
    }

    private fun clickNavigateToLicenses() {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLicensesFragment())
    }

    private fun setupObservers() {

        safeUiLaunch {
            viewModel.state.collect {
                when (it) {
                    is SettingsState.ShowThemesView -> showThemeDialog(it)
                    is SettingsState.Success -> stateSuccess(it)
                    else -> {}
                }
            }
        }
    }

    interface DialogListener {
        fun onItemSelected(value: Int)
    }

    private fun <T : IsOption> showOptionDialog(
        title: String,
        list: List<T>,
        current: T,
        listener: DialogListener
    ) {
        val builder =
            context?.let { MaterialAlertDialogBuilder(it) } ?: return
        // setup the alert builder
        builder.setTitle(title)

        val items = arrayOfNulls<String>(list.size)

        for (i in list.indices) {
            items[i] = (list[i] as IsOption).getName()
        }

        val checkedItem: Int = current.getValue().toInt()

        builder.setSingleChoiceItems(
            items, checkedItem
        ) { dialog: DialogInterface?, which: Int -> }

        // add OK and Cancel buttons
        var unitChosed: Int = current.getValue().toInt()

        // add OK and Cancel buttons
        builder.setPositiveButton(getString(R.string.all_ok)) { dialog, which ->
            val selectedPosition = (dialog as AlertDialog).listView
                .checkedItemPosition
            unitChosed = selectedPosition
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.all_cancel), null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()

//        val buttonNegative: Button = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
//        buttonNegative.backgroundTintList = resources.getColorStateList(R.color.colorPrimary)
//        buttonNegative.setTextColor(resources.getColor(R.color.colorAccent))
//
//        val buttonPositive: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
//        buttonPositive.backgroundTintList = resources.getColorStateList(R.color.colorPrimary)
//        buttonPositive.setTextColor(resources.getColor(R.color.colorAccent))

        dialog.setOnDismissListener {
            listener.onItemSelected(unitChosed)
        }
    }


    private fun stateSuccess(it: SettingsState.Success) {
        binding.settingsTextTheme.text = it.themeMode.getName()
    }

    private fun showThemeDialog(it: SettingsState.ShowThemesView) {
        val title = getString(R.string.settings_choose_theme)
        showOptionDialog(
            title,
            it.list,
            it.current,
            object : DialogListener {
                override fun onItemSelected(value: Int) {
                    viewModel.saveAndSetTheme(ThemeMode.getThemeFromType(value))
                }
            })
    }


    private fun clickChooseTheme() {
        viewModel.clickShowTheme()
    }
}