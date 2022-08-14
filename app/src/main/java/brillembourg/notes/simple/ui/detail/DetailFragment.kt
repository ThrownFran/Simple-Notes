package brillembourg.notes.simple.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.ui.extras.hideKeyboard
import brillembourg.notes.simple.ui.extras.showMessage
import brillembourg.notes.simple.ui.extras.showSoftKeyboard
import brillembourg.notes.simple.ui.extras.themeColor
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import com.google.android.material.transition.platform.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
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

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            // Scope the transition to a view in the hierarchy so we know it will be added under
            // the bottom app bar but over the elevation scale of the exiting HomeFragment.
            drawingViewId = R.id.fragment_container_view
            duration = 300
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))
        }


        setupBackPhysicalButtonListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        postponeEnterTransition()
//        view?.doOnPreDraw { startPostponedEnterTransition() }


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
                    binding.detailLinear.requestFocus()
                }
            })
    }

    private fun clickBack() {
        viewModel.onBackPressed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is DetailState.CreateTask -> onStateNewTask()
                is DetailState.TaskLoaded -> onStateTaskLoaded(it)
                is DetailState.TaskCreated -> onStateTaskCreated(it)
                is DetailState.TaskSaved -> onStateTaskSaved(it)
                is DetailState.ExitWithoutSaving -> finishView()
            }
        }
    }

    private fun onStateTaskCreated(it: DetailState.TaskCreated) {
        showMessage(it.message)
        finishView()
    }

    private fun onStateNewTask() {
        focusKeyboard()
    }

    private fun focusKeyboard() {
        binding.detailEditContent.apply {
            requestFocus()
            showSoftKeyboard()
        }
    }

    private fun onStateTaskLoaded(it: DetailState.TaskLoaded) {
        setupContent(it.task)
        setupTitle(it.task.title)
        //TODO unfocus title
        binding.detailLinear.requestFocus()
        binding.detailEditContent.hideKeyboard()
    }

    private fun setupTitle(title: String?) {
        binding.detailEditTitle.setText(title)
    }

    private fun onStateTaskSaved(it: DetailState.TaskSaved) {
        showMessage(it.message)
        finishView()
    }

    private fun finishView() {
        findNavController().popBackStack()
    }

    private fun setupContent(task: TaskPresentationModel) {
        binding.detailEditContent.setText(task.content)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
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