package brillembourg.notes.simple.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.ui.TaskPresentationModel
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
                viewModel.saveTask()
            }
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
//        setupBackButton()
    }

//    // This callback will only be called when MyFragment is at least Started.
//    private fun setupBackButton() {
//        // This callback will only be called when MyFragment is at least Started.
//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true /* enabled by default */) {
//                override fun handleOnBackPressed() {
//                    // Handle the back button event
//                    viewModel.saveTask()
//                }
//            }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
//
////        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            // Handle the back button event
////            viewModel.saveTask()
////        }
//    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is DetailState.TaskLoaded -> onStateTaskLoaded(it)
                is DetailState.TaskSaved -> onStateTaskSaved(it)
            }
        }
    }

    private fun onStateTaskLoaded(it: DetailState.TaskLoaded) {
        setupContent(it.task)
    }

    private fun onStateTaskSaved(it: DetailState.TaskSaved) {
        showToast(it.message)
        findNavController().popBackStack()
    }

    private fun showToast(message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    private fun setupContent(task: TaskPresentationModel) {
        binding.detailEdit.setText(task.content)
    }


}