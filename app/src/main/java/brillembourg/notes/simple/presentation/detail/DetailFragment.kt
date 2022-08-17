package brillembourg.notes.simple.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.presentation.extras.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
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
        setEditNoteEnterTransition()
        setupBackPhysicalButtonListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


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
//        returnTransition = Slide().apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
//            addTarget(R.id.home_fab)
//        }

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
        binding.detailLinear.clearFocus()
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