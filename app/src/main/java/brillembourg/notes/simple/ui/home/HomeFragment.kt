package brillembourg.notes.simple.ui.home

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import brillembourg.notes.simple.databinding.FragmentMainBinding
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.ui.TaskPresentationModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private var recylerViewState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun setupObservers() {
        viewModel.navigateToDetail.observe(viewLifecycleOwner) {
            navigateToDetail(it)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is HomeState.Loading -> {

                }
                is HomeState.TaskListError -> {}
                is HomeState.TaskListSuccess -> {
                    setupTaskList(it)
                }
            }
        }
    }

    private fun navigateToDetail(it: Task) {
        //navigate to detail fragment
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.task = TaskPresentationModel(it.id, it.content, it.date)
        findNavController().navigate(directions)
    }

    private fun setupTaskList(it: HomeState.TaskListSuccess) {
        binding.homeRecycler.apply {
            adapter = TaskAdapter(it.taskList) {
                viewModel.clickItem(it)
            }
            layoutManager = LinearLayoutManager(context).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
        }
    }

    private fun retrieveRecyclerStateIfApplies(layoutManager: LinearLayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}