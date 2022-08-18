package brillembourg.notes.simple.presentation.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.extras.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

@AndroidEntryPoint
class DetailFragment : Fragment() {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditNoteEnterTransition()
        setupBackPhysicalButtonListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e("DetailFragment", "OnCreateView")
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        setHasOptionsMenu(true)
        unfocusScreenWhenKeyboardHidden()
        return binding.root
    }


    private fun unfocusScreenWhenKeyboardHidden() {
        setEventListener(
            requireActivity(),
            KeyboardVisibilityEventListener {
                // Ah... at last. do your thing :)
                if (!it) {
                    binding.detailEditTitle.clearFocus()
                    binding.detailEditContent.clearFocus()
                    binding.detailLinear.clearFocus()
                }
            })
    }

    private fun clickBack() {
        viewModel.onBackPressed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareTransition(view)
        setCreateNoteEnterTransition(
            myStartView = requireActivity().findViewById(R.id.home_fab),
            myEndView = binding.detailLinear
        )
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiDetailUiState.collect { uiState ->

                    setupTitleAndContent(uiState.userInput)

                    setToolbarTitle(uiState.isNewTask)

                    if (uiState.unFocusInput) {
                        unFocus()
                        viewModel.onUnFocusCompleted()

                    }

                    if (uiState.focusInput) {
                        focus()
                        viewModel.onFocusCompleted()
                    }

                    if (uiState.userMessage != null) {
                        showMessage(uiState.userMessage) {
                            viewModel.onMessageShown()
                        }
                    }

                    if (uiState.navigateBack) {
                        finishView()
                    }

                }
            }
        }
    }

    private fun setToolbarTitle(isNewTask: Boolean) {
        if (isNewTask) {
            //Create or Edit title
            val activityBinding = (activity as MainActivity?)?.binding
            activityBinding?.toolbar?.title = "Add note"
        } else {
            val activityBinding = (activity as MainActivity?)?.binding
            activityBinding?.toolbar?.title = "Edit note"
        }
    }

    private fun setupTitleAndContent(userInput: UserInput) {
        if (userInput.isNotEmpty()) {
            setupTitle(userInput.title)
            setupContent(userInput.content)
        }
    }

    private fun focus() {
        binding.detailEditContent.apply {
            requestFocus()
            showSoftKeyboard()
        }
    }

    private fun unFocus() {
        binding.detailLinear.clearFocus()
        binding.detailEditContent.hideKeyboard()
    }

    private fun setupTitle(title: String?) {
        if (title == binding.detailEditTitle.toString()) return

        binding.detailEditTitle.setText(title)
    }

    private fun setupContent(content: String?) {
        if (content == binding.detailEditContent.toString()) return

        binding.detailEditContent.setText(content)
    }

    private fun finishView() {
        findNavController().popBackStack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                clickBack()
                return true
            }
        }
        return false
    }

    private fun setupBackPhysicalButtonListener() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                clickBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


}