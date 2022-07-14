package brillembourg.notes.simple.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import brillembourg.notes.simple.ui.extras.showSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailFragment : Fragment() {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentDetailBinding.inflate(inflater,container,false)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                viewModel.onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is DetailState.TaskLoaded -> onStateTaskLoaded(it)
                is DetailState.TaskSaved -> onStateTaskSaved(it)
                is DetailState.ExitWithoutSaving -> finishView()
            }
        }

        binding.detailEdit.apply {
            requestFocus()
            showSoftKeyboard()
        }
    }

    private fun onStateTaskLoaded(it: DetailState.TaskLoaded) {
        setupContent(it.task)
    }

    private fun onStateTaskSaved(it: DetailState.TaskSaved) {
        showToast(it.message)
        finishView()
    }

    private fun finishView() {
        findNavController().popBackStack()
    }

    private fun showToast(message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    private fun setupContent(task: TaskPresentationModel) {
        binding.detailEdit.setText(task.content)
    }


}