package brillembourg.notes.simple.ui.home

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentMainBinding
import brillembourg.notes.simple.ui.extras.showToast
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private var recylerViewState: Parcelable? = null

    private var isStaggered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
//        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
        viewModel.getTaskList()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home,menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                clickVerticalLayout()
                return true
            }
            R.id.menu_home_staggered -> {
                clickStaggeredLayout()
                return true
            }

        }
        return false
    }

    private fun clickStaggeredLayout() {
        viewModel.getTaskList()
        isStaggered = true
//        binding.homeRecycler.apply {
//            layoutManager = buildStaggeredManager().also { layoutManager ->
//                retrieveRecyclerStateIfApplies(layoutManager)
//            }
//            adapter?.notifyDataSetChanged()
//            viewModel.getTaskList()
//            isStaggered = true
//        }
    }

    private fun clickVerticalLayout() {
        viewModel.getTaskList()
        isStaggered = false
//        binding.homeRecycler.apply {
//            layoutManager = buildLinearManager().also { layoutManager ->
//                retrieveRecyclerStateIfApplies(layoutManager)
//            }
//            adapter?.notifyDataSetChanged()
//            viewModel.getTaskList()
//            isStaggered = false
//        }
    }

    private fun RecyclerView.buildLinearManager() =
        LinearLayoutManager(context)


    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun setupObservers() {
        viewModel.navigateToDetailEvent.observe(viewLifecycleOwner) {
            navigateToDetail(it)
        }

        viewModel.navigateToCreateEvent.observe(viewLifecycleOwner) {
            navigateToCreateTask()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is HomeState.Loading -> {

                }
                is HomeState.TaskListError -> {
                    showMessage(it.message)
                }
                is HomeState.TaskListSuccess -> {
                    setupTaskList(it)
                }
            }
        }

        viewModel.messageEvent.observe(viewLifecycleOwner) {
            showMessage(it)
        }
    }

    private fun showMessage(message: String) {
        context?.showToast(message)
    }

    private fun navigateToCreateTask() {
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        findNavController().navigate(directions)
    }

    private fun navigateToDetail(it: TaskPresentationModel) {
        //navigate to detail fragment
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.task = it
        findNavController().navigate(directions)
    }

    private fun setupTaskList(it: HomeState.TaskListSuccess) {
        binding.homeRecycler.apply {
            adapter = TaskAdapter(it.taskList,
                onLongClick = {
                    viewModel.longClick(it)
                }, onClick = {
                    viewModel.clickItem(it)
                })
            layoutManager = if(isStaggered) buildStaggeredManager() else buildLinearManager().also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }

//            layoutManager = LinearLayoutManager(context).also { layoutManager ->
//                retrieveRecyclerStateIfApplies(layoutManager)
//            }
        }
    }

    private fun RecyclerView.buildStaggeredManager() = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}