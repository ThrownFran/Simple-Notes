package brillembourg.notes.simple.presentation.trash

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentTrashBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.extras.*
import brillembourg.notes.simple.presentation.home.HomeFragment
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TrashFragment : Fragment(), MenuProvider {

    companion object TrashFragment {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: TrashViewModel by viewModels()

    private var _binding: FragmentTrashBinding? = null
    private lateinit var binding: FragmentTrashBinding

    private var recylerViewState: Parcelable? = null
    private var actionMode: ActionMode? = null

    private var layoutType = LayoutType.Vertical
    private var isAnimatingTaskPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) _binding = FragmentTrashBinding.inflate(inflater, container, false)
        binding = _binding as FragmentTrashBinding
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
        unlockToolbarScrolling()
        animateFabWithRecycler()
    }

    private fun animateFabWithRecycler() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.homeFab?.animateWithRecycler(binding.trashRecycler)
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
        val taskAdapter = recyclerView.adapter as ArchivedTaskAdapter

        changeLayout(
            recyclerView,
            layoutType,
            taskAdapter.currentList
        )
    }

    private fun clickStaggeredLayout() {
        clickChangeLayout(binding.trashRecycler, LayoutType.Grid)
    }

    private fun clickVerticalLayout() {
        clickChangeLayout(binding.trashRecycler, LayoutType.Vertical)
    }

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun setupObservers() {
        viewModel.navigateToDetailEvent.observe(viewLifecycleOwner) {
//            navigateToDetail(it)
            //TODO
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is TrashState.Loading -> {

                }
                is TrashState.ShowError -> {
                    showMessage(it.message)
                }
            }
        }

        viewModel.messageEvent.observe(viewLifecycleOwner) {
            showMessage(it)
        }

//        viewModel.observeTaskList().observe(viewLifecycleOwner) {
//            setupTaskList(it)
//        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.taskList.collect {
                    setupTaskList(it)
                }
            }
        }

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
        val directions = TrashFragmentDirections.actionTrashFragmentToDetailFragment()
        directions.task = it


        isAnimatingTaskPosition = (binding.trashRecycler.adapter as ArchivedTaskAdapter)
            .currentList.indexOf(it)

        setTransitionToEditNote()
        findNavController().navigate(directions, setupExtrasToDetail(view))
    }

    private fun setupTaskList(taskList: List<TaskPresentationModel>) {
        if (binding.trashRecycler.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(binding.trashRecycler.adapter as ArchivedTaskAdapter, taskList)
        }
    }

    private fun setupTaskRecycler(taskList: List<TaskPresentationModel>) {
        binding.trashRecycler.apply {
            adapter = buildTaskAdapter(this, taskList)
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
        }
    }

    private fun updateListAndNotify(
        taskAdapter: ArchivedTaskAdapter,
        taskList: List<TaskPresentationModel>
    ) = with(taskAdapter) {

        submitListAndScrollIfApplies(taskAdapter, currentList, taskList)

        //Notify or update possible changes
        if (isExitingFromDetailScreen()) {
            val taskModel = currentList[isAnimatingTaskPosition]
            notifyItemChanged(isAnimatingTaskPosition, taskModel)
            exitFromDetailFinished()
        } else {
            notifyDataSetChanged()
        }
    }

    private fun submitListAndScrollIfApplies(
        taskAdapter: ArchivedTaskAdapter,
        currentList: List<TaskPresentationModel>,
        taskList: List<TaskPresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        taskAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
    }


    private fun isExitingFromDetailScreen() = isAnimatingTaskPosition != -1

    private fun exitFromDetailFinished() {
        isAnimatingTaskPosition = -1
    }


    private fun scrollToTop() {
        binding.trashRecycler.scrollToPosition(0)
    }

    private fun buildTaskAdapter(
        recyclerView: RecyclerView,
        taskList: List<TaskPresentationModel>
    ): ArchivedTaskAdapter {

        return ArchivedTaskAdapter(
            recyclerView,
            onSelection = {
                launchContextualActionBar()
            },
            onClick = { task, clickedView ->
                clickItem(task, clickedView)
            })
            .also {
                it.submitList(taskList)
            }
    }

    private fun launchContextualActionBar() {
        actionMode = setupContextualActionBar(
            toolbar = requireActivity().findViewById(R.id.toolbar),
            menuId = R.menu.menu_context_trash,
            currentActionMode = actionMode,
            adapter = binding.trashRecycler.adapter as ArchivedTaskAdapter,
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
            clickDeleteTasks(getSelectedItems())
            true
        }
        R.id.menu_context_menu_unarchive -> {
            clickUnarchiveTasks(getSelectedItems())
            true
        }
        else -> false
    }

    private fun clickUnarchiveTasks(selectedItems: List<TaskPresentationModel>) {
        viewModel.unarchiveTasks(selectedItems)
        actionMode?.finish()
    }

    private fun getSelectedItems() = (binding.trashRecycler
        .adapter as ArchivedTaskAdapter).currentList.filter { it.isSelected }


    private fun clickItem(it: TaskPresentationModel, view: View) {
//        viewModel.clickItem(it)
        navigateToDetail(it, view)
    }

    private fun clickDeleteTasks(taskList: List<TaskPresentationModel>) {
        if (taskList.isEmpty()) throw IllegalArgumentException("Nothing to delete but trash was pressed")

        val title =
            if (taskList.size <= 1) getString(R.string.delete_task_permanently) else getString(R.string.delete_tasks_permanently)

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_delete_dark_24)
//            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.all_cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.all_delete)) { dialog, which ->
                viewModel.clickDeleteTasks(taskList)
                actionMode?.finish()
            }
            .show()

    }


    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.trashRecycler.layoutManager?.onSaveInstanceState()
    }
}