package brillembourg.notes.simple.presentation.home

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentHomeBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.base.MainUiState
import brillembourg.notes.simple.presentation.base.MainViewModel
import brillembourg.notes.simple.presentation.extras.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.getNoteSelectedTitle
import brillembourg.notes.simple.presentation.ui_utils.setupContextualActionBar
import brillembourg.notes.simple.presentation.ui_utils.setupDragAndDropTouchHelper
import brillembourg.notes.simple.util.UiText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding: FragmentHomeBinding

    private var recylerViewState: Parcelable? = null
    private var actionMode: ActionMode? = null

    private var layoutType = LayoutType.Vertical

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = _binding as FragmentHomeBinding
        binding.viewmodel = viewModel
        Log.e("HomeFragment", "OnCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
        unlockToolbarScrolling()
        animateFabWithRecycler()
        Log.e("HomeFragment", "OnViewCreated")
    }

    private fun animateFabWithRecycler() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.homeFab?.animateWithRecycler(binding.homeRecycler)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_home_vertical).apply { isVisible = layoutType == LayoutType.Grid }
        menu.findItem(R.id.menu_home_staggered)
            .apply { isVisible = layoutType == LayoutType.Vertical }
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

    private fun clickChangeLayout(
        recyclerView: RecyclerView,
        layoutType: LayoutType
    ) {
        this.layoutType = layoutType
        val taskAdapter = recyclerView.adapter as TaskAdapter

        changeLayout(
            recyclerView,
            layoutType,
            taskAdapter.currentList
        )

        taskAdapter.itemTouchHelper =
            taskAdapter.setupDragAndDropTouchHelper(getDragDirs(layoutType)).also {
                it.attachToRecyclerView(recyclerView)
            }
    }

    private fun clickStaggeredLayout() {
        clickChangeLayout(binding.homeRecycler, LayoutType.Grid)
    }

    private fun clickVerticalLayout() {
        clickChangeLayout(binding.homeRecycler, LayoutType.Vertical)
    }

    override fun onDestroyView() {
        saveRecyclerState()
        Log.e("HomeFragment", "OnDestroyView")
        super.onDestroyView()
    }

    private fun setupObservers() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.mainUiState.collect { mainUiState ->
                    setupNavigateToCreateTask(mainUiState)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeUiState.collect { homeUiState: HomeUiState ->
                    setupMessageObserver(homeUiState.userMessage)
                    setupNavigateToDetailObserver(homeUiState.navigateToDetail)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.taskListState.collect {
                    setupTaskList(it)
                }
            }
        }


    }

    private fun setupNavigateToCreateTask(mainUiState: MainUiState) {
        if (mainUiState.navigateToCreateTask) {
            navigateToCreateTask()
            activityViewModel.onNavigateToCreateTaskCompleted()
        }
    }

    private fun setupMessageObserver(userMessage: UiText?) {
        userMessage?.let {
            showMessage(it) {
                viewModel.onMessageShown()
            }
        }
    }

    private fun setupNavigateToDetailObserver(navigateToDetail: NavigateToTaskDetailEvent) {
        if (navigateToDetail.mustConsume) {
            val view =
                binding.homeRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!!)!!.itemView
            navigateToDetail(navigateToDetail.taskPresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun navigateToCreateTask() {
        lockToolbarScrolling()
        finishSelectionActionModeIfActive()
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        setTransitionToCreateNote()
        findNavController().navigate(directions)
    }


    private fun unlockToolbarScrolling() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.toolbar?.unLockScroll()
    }

    private fun lockToolbarScrolling() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.toolbar?.lockScroll()
    }

    private fun finishSelectionActionModeIfActive() {
        actionMode?.finish()
        actionMode = null
    }

    private fun navigateToDetail(it: TaskPresentationModel, view: View) {
        lockToolbarScrolling()
        finishSelectionActionModeIfActive()

        //navigate to detail fragment
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.task = it

        setTransitionToEditNote()
        findNavController().navigate(directions, setupExtrasToDetail(view))
    }

    private fun setupTaskList(taskList: List<TaskPresentationModel>) {
        if (binding.homeRecycler.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(binding.homeRecycler.adapter as TaskAdapter, taskList)
        }
    }

    private fun setupTaskRecycler(taskList: List<TaskPresentationModel>) {
        binding.homeRecycler.apply {
            adapter = buildTaskAdapter(this, taskList, getDragDirs(layoutType))
            layoutManager = buildLayoutManager(layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun updateListAndNotify(
        taskAdapter: TaskAdapter,
        taskList: List<TaskPresentationModel>
    ) {
        submitListAndScrollIfApplies(taskAdapter, taskAdapter.currentList, taskList)
    }

    private fun submitListAndScrollIfApplies(
        taskAdapter: TaskAdapter,
        currentList: List<TaskPresentationModel>,
        taskList: List<TaskPresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        taskAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
    }

    private fun scrollToTop() {
        binding.homeRecycler.scrollToPosition(0)
    }

    private fun buildTaskAdapter(
        recyclerView: RecyclerView,
        taskList: List<TaskPresentationModel>,
        dragDirs: Int
    ): TaskAdapter {

        return TaskAdapter(
            dragDirs,
            recyclerView,
            onSelection = {
                launchContextualActionBar()
            },
            onClick = { task, clickedView ->
                clickItem(task, clickedView)
            },
            onReorderSuccess = { tasks ->
                clickReorder(tasks)
            },
            onReorderCanceled = {
                clickReorderCancelled()
            })
            .also {
                it.submitList(taskList)
                it.itemTouchHelper.attachToRecyclerView(recyclerView)
            }
    }

    private fun launchContextualActionBar() {
        actionMode = setupContextualActionBar(
            toolbar = requireActivity().findViewById(R.id.toolbar),
            menuId = R.menu.menu_context_home,
            currentActionMode = actionMode,
            adapter = binding.homeRecycler.adapter as TaskAdapter,
            onActionClick = { onContextualActionItem(menuId = it) },
            onSetTitle = { selectedSize: Int ->
                getNoteSelectedTitle(
                    resources = resources,
                    selectedSize = selectedSize
                )
            },
            onDestroyMyActionMode = { actionMode = null }
        )
    }

    private fun onContextualActionItem(menuId: Int) = when (menuId) {
        R.id.menu_context_menu_delete -> {
            clickDeleteTasks((binding.homeRecycler
                .adapter as TaskAdapter).currentList.filter { it.isSelected })
            true
        }
        else -> false
    }

    private fun clickReorder(tasks: List<TaskPresentationModel>) {
        actionMode?.finish()
        viewModel.onReorderedTaskList(tasks)
    }

    private fun clickReorderCancelled() {
        actionMode?.finish()
    }

    private fun clickItem(it: TaskPresentationModel, view: View) {
        viewModel.onTaskClick(it)
    }

    private fun clickDeleteTasks(taskList: List<TaskPresentationModel>) {
        if (taskList.isEmpty()) throw IllegalArgumentException("Nothing to delete but trash was pressed")

        val title =
            if (taskList.size > 1) getString(R.string.move_tasks_to_trash) else getString(R.string.move_task_to_trash)

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_delete_dark_24)
//            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.all_cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.all_move_to_trash)) { dialog, which ->
                viewModel.deleteTasks(taskList)
                actionMode?.finish()
            }
            .show()

    }


    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}