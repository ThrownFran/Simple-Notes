package brillembourg.notes.simple.presentation.trash

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentTrashBinding
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.extras.animateWithRecycler
import brillembourg.notes.simple.presentation.extras.safeUiLaunch
import brillembourg.notes.simple.presentation.extras.setTransitionToEditNote
import brillembourg.notes.simple.presentation.extras.setupExtrasToDetail
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrashFragment : Fragment(), MenuProvider {

    companion object TrashFragment {
        fun newInstance() = TrashFragment()
    }

    private val viewModel: TrashViewModel by viewModels()

    private var _binding: FragmentTrashBinding? = null
    private lateinit var binding: FragmentTrashBinding

    private var recylerViewState: Parcelable? = null
    private var actionMode: ActionMode? = null

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
        renderStates()
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
        val layoutType = viewModel.trashUiState.value.noteLayout.toLayoutType()
        menu.findItem(R.id.menu_home_vertical)
            .apply { isVisible = layoutType == LayoutType.Staggered }
        menu.findItem(R.id.menu_home_staggered)
            .apply { isVisible = layoutType == LayoutType.LinearVertical }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val menuHost = requireActivity()
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                viewModel.onLayoutChange(NoteLayout.Vertical)
                menuHost.invalidateMenu()
                return true
            }
            R.id.menu_home_staggered -> {
                viewModel.onLayoutChange(NoteLayout.Grid)
                menuHost.invalidateMenu()
                return true
            }
        }
        return false
    }

//    private fun clickChangeLayout(
//        recyclerView: RecyclerView,
//        layoutType: LayoutType
//    ) {
//        val taskAdapter: ArchivedTaskAdapter? = recyclerView.adapter as ArchivedTaskAdapter?
//
//        changeLayout(
//            recyclerView,
//            layoutType,
//            taskAdapter?.currentList
//        )
//    }

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun renderStates() {

        safeUiLaunch {
            viewModel.trashUiState.collect { uiState: TrashUiState ->

                setupNoteList(uiState.taskList)

                selectionModeState(uiState.selectionModeActive)

                navigateToDetailState(uiState.navigateToEditNote)

                showArchiveConfirmationState(uiState.showArchiveNotesConfirmation)

                noteLayoutState(uiState.noteLayout)

            }
        }
    }

    private fun noteLayoutState(noteLayout: NoteLayout) {
        val taskAdapter: ArchivedTaskAdapter? =
            binding.trashRecycler.adapter as ArchivedTaskAdapter?

        changeLayout(
            binding.trashRecycler,
            noteLayout.toLayoutType(),
            taskAdapter?.currentList
        )
    }

    private fun showArchiveConfirmationState(showDeleteConfirmationState: TrashUiState.ShowDeleteNotesConfirmation?) {
        showDeleteConfirmationState?.let {
            showDeleteTasksDialog(showDeleteConfirmationState.tasksToDeleteSize) {
                viewModel.onDismissConfirmDeleteShown()
            }
        }
    }


    private fun selectionModeState(selectionModeActive: TrashUiState.SelectionModeActive?) {
        if (selectionModeActive == null) {
            actionMode?.finish()
            actionMode = null
            return
        }

        launchContextualActionBar(selectionModeActive.size)
    }

    private fun navigateToDetailState(navigateToDetail: TrashUiState.NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val view =
                binding.trashRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!!)!!.itemView
            navigateToDetail(navigateToDetail.taskPresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun navigateToDetail(it: TaskPresentationModel, view: View) {
        //navigate to detail fragment
        val directions = TrashFragmentDirections.actionTrashFragmentToDetailFragment()
        directions.task = it

        setTransitionToEditNote()
        findNavController().navigate(directions, setupExtrasToDetail(view))
    }

    private fun setupNoteList(taskList: List<TaskPresentationModel>) {
        if (binding.trashRecycler.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(binding.trashRecycler.adapter as ArchivedTaskAdapter, taskList)
        }
    }

    private fun setupTaskRecycler(taskList: List<TaskPresentationModel>) {
        binding.trashRecycler.apply {
            adapter = buildTaskAdapter(this, taskList)

            val layoutType = viewModel.trashUiState.value.noteLayout.toLayoutType()
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            isNestedScrollingEnabled = true
        }
    }

    private fun updateListAndNotify(
        taskAdapter: ArchivedTaskAdapter,
        taskList: List<TaskPresentationModel>
    ) {
        submitListAndScrollIfApplies(taskAdapter, taskList)
    }

    private fun submitListAndScrollIfApplies(
        taskAdapter: ArchivedTaskAdapter,
        taskList: List<TaskPresentationModel>
    ) {
        val currentList = taskAdapter.currentList
        val isInsertingInList = currentList.size < taskList.size
        taskAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
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
                onNoteSelection()
            },
            onClick = { task, clickedView ->
                onNoteClicked(task, clickedView)
            })
            .apply {
                submitList(taskList)
            }
    }

    private fun onNoteSelection() {
        viewModel.onSelection()
    }

    private fun launchContextualActionBar(sizeSelected: Int) {
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
            onDestroyMyActionMode = { viewModel.onSelectionDismissed() }
        )
    }

    private fun onContextualActionItem(menuId: Int) = when (menuId) {
        R.id.menu_context_menu_delete -> {
            viewModel.onShowConfirmDeleteNotes()
            true
        }
        R.id.menu_context_menu_unarchive -> {
            onUnarchiveTasks()
            true
        }
        else -> false
    }

    private fun onDeleteNotes() {
        viewModel.onDeleteNotes()
    }

    private fun onUnarchiveTasks() {
        viewModel.onUnarchiveTasks()
    }


    private fun onNoteClicked(it: TaskPresentationModel, view: View) {
        viewModel.onNoteClick(it)
    }

    private fun showDeleteTasksDialog(
        size: Int,
        onDismiss: () -> Unit
    ) {

        val title =
            if (size <= 1) getString(R.string.delete_task_permanently) else getString(R.string.delete_tasks_permanently)

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_delete_dark_24)
//            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.all_cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.all_delete)) { dialog, which ->
                onDeleteNotes()
            }.setOnDismissListener {
                onDismiss.invoke()
            }
            .showWithLifecycle(viewLifecycleOwner)
    }


    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.trashRecycler.layoutManager?.onSaveInstanceState()
    }


}