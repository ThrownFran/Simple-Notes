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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        val inflater: MenuInflater? = activity?.menuInflater
//        inflater?.inflate(R.menu.context_menu, menu)
//    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_context_menu_delete -> {
                clickDeleteTask()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun clickDeleteTask() {
        val adapter = (binding.homeRecycler.adapter as TaskAdapter)
        adapter.currentPosition?.let {
            viewModel.clickDeleteTask(adapter.currentList[it])
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_home_vertical).apply { isVisible = isStaggered }
        menu.findItem(R.id.menu_home_staggered).apply { isVisible = !isStaggered }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val menuHost = requireActivity()
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                clickVerticalLayout()
                menuHost.invalidateMenu()
                return true
            }
            R.id.menu_home_staggered -> {
                clickStaggeredLayout()
                menuHost.invalidateMenu()
                return true
            }
        }
        return false
    }

    private fun clickStaggeredLayout() {
        isStaggered = true
        binding.homeRecycler.apply {
            layoutManager = buildStaggeredManager()
            adapter?.notifyDataSetChanged()
        }
    }

    private fun clickVerticalLayout() {
        isStaggered = false
        binding.homeRecycler.apply {
            layoutManager = buildLinearManager()
            adapter?.notifyDataSetChanged()
        }
    }

    private fun buildLinearManager() =
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
                is HomeState.ShowError -> {
                    showMessage(it.message)
                }
            }
        }

        viewModel.messageEvent.observe(viewLifecycleOwner) {
            showMessage(it)
        }

        viewModel.observeTaskList().observe(viewLifecycleOwner) {
            setupTaskList(it)
        }

//        lifecycleScope.launchWhenStarted {
//            viewModel.observeTaskList().collect {
//                setupTaskList(it)
////                it.toString()
////                val adapter = binding.homeRecycler.adapter as TaskAdapter?
////                adapter?.submitList(it)
//            }
//        }
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

    private fun setupTaskList(taskList: List<TaskPresentationModel>) {
        if (binding.homeRecycler.adapter == null) {
            binding.homeRecycler.apply {
                buildAdapter(taskList)
            }
        } else {
            (binding.homeRecycler.adapter as TaskAdapter).submitList(taskList)
        }

    }

    private fun RecyclerView.buildAdapter(taskList: List<TaskPresentationModel>) {
        layoutManager =
            if (isStaggered) buildStaggeredManager() else buildLinearManager()
                .also { layoutManager ->
                    retrieveRecyclerStateIfApplies(layoutManager)
                }

        adapter = TaskAdapter(requireActivity().menuInflater,
            binding.homeRecycler,
            onSelection = {
//                          startActionMode(ActionMode.Callback() {
//
//                          })
//                viewModel.clickDeleteTask(it)
            },
            onClick = {
                viewModel.clickItem(it)
            },
            onReorder = {
                viewModel.reorderList(it)
            })
            .also {
                it.submitList(taskList)
                it.itemTouchHelper.attachToRecyclerView(this)
            }
    }

    private fun buildStaggeredManager() =
        StaggeredGridLayoutManager(2, RecyclerView.VERTICAL).also {
            it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        }

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}