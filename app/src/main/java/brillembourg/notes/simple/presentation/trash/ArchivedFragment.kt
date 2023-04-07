package brillembourg.notes.simple.presentation.trash

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
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
import brillembourg.notes.simple.presentation.custom_views.*
import brillembourg.notes.simple.presentation.detail.setupExtrasToDetail
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.getNoteSelectedTitle
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.buildLayoutManager
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.changeLayout
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.toLayoutType
import brillembourg.notes.simple.presentation.ui_utils.setupContextualActionBar
import brillembourg.notes.simple.presentation.ui_utils.showDeleteTasksDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ArchivedFragment : Fragment(), MenuProvider {

    companion object ArchivedFragment {
        fun newInstance() = ArchivedFragment()
    }

    private val viewModel: ArchivedViewModel by viewModels()

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
        val layoutType = viewModel.archivedUiState.value.noteLayout.toLayoutType()
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

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun renderStates() {

        safeUiLaunch {
            viewModel.archivedUiState.collect { uiState: ArchivedUiState ->

                setupNoteList(uiState.noteList)

                selectionModeState(uiState.selectionModeActive)

                navigateToDetailState(uiState.navigateToEditNote)

                showArchiveConfirmationState(uiState.showArchiveNotesConfirmation)

                noteLayoutState(uiState.noteLayout)

                copyClipboardState(uiState.copyToClipboard)

                shareNotesAsStringState(uiState.shareNoteAsString)

            }
        }
    }

    private fun shareNotesAsStringState(shareNoteAsString: String?) {
        shareNoteAsString?.let {
            shareText(shareNoteAsString)
            viewModel.onShareCompleted()
        }
    }

    private fun copyClipboardState(copyToClipboard: String?) {
        copyToClipboard?.let {
            copy(it)
            viewModel.onCopiedCompleted()
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

    private fun showArchiveConfirmationState(showDeleteConfirmationState: ArchivedUiState.ShowDeleteNotesConfirmation?) {
        showDeleteConfirmationState?.let {
            showDeleteTasksDialog(
                fragment = this,
                size = showDeleteConfirmationState.tasksToDeleteSize,
                onPositive = {
                    onDeleteNotes()
                },
                onDismiss = {
                    viewModel.onDismissConfirmDeleteShown()
                })
        }
    }


    private fun selectionModeState(selectionModeActive: ArchivedUiState.SelectionModeActive?) {
        if (selectionModeActive == null) {
            actionMode?.finish()
            actionMode = null
            return
        }

        launchContextualActionBar(selectionModeActive.size)
    }

    private fun navigateToDetailState(navigateToDetail: ArchivedUiState.NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val view =
                binding.trashRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!!)!!.itemView
            navigateToDetail(navigateToDetail.notePresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun navigateToDetail(it: NotePresentationModel, view: View) {
        //navigate to detail fragment
        val directions = ArchivedFragmentDirections.actionTrashFragmentToDetailFragment()
        directions.task = it

        setTransitionToEditNote()
        findNavController().navigate(directions, setupExtrasToDetail(view))
    }

    private fun setupNoteList(taskList: List<NotePresentationModel>) {
        if (binding.trashRecycler.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(binding.trashRecycler.adapter as ArchivedTaskAdapter, taskList)
        }

        binding.trashTextEmpty.isVisible = taskList.isEmpty()
    }

    private fun setupTaskRecycler(taskList: List<NotePresentationModel>) {
        binding.trashRecycler.apply {
            adapter = buildTaskAdapter(this, taskList)

            val layoutType = viewModel.archivedUiState.value.noteLayout.toLayoutType()
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            isNestedScrollingEnabled = true
            adapter?.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    private fun updateListAndNotify(
        taskAdapter: ArchivedTaskAdapter,
        taskList: List<NotePresentationModel>
    ) {
        submitListAndScrollIfApplies(taskAdapter, taskList)
    }

    private fun submitListAndScrollIfApplies(
        taskAdapter: ArchivedTaskAdapter,
        taskList: List<NotePresentationModel>
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
        taskList: List<NotePresentationModel>
    ): ArchivedTaskAdapter {

        return ArchivedTaskAdapter(
            recyclerView,
            onSelection = {
                onNoteSelection()
            },
            onClick = { task ->
                onNoteClicked(task)
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
            menuId = R.menu.menu_contextual_trash,
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

        R.id.menu_context_share -> {
            onShareNotes()
            true
        }


        else -> false
    }


    private fun onShareNotes() {
        viewModel.onShare()
    }

    private fun onDeleteNotes() {
        viewModel.onDeleteNotes()
    }

    private fun onUnarchiveTasks() {
        viewModel.onUnarchiveTasks()
    }


    private fun onNoteClicked(it: NotePresentationModel) {
        viewModel.onNoteClick(it)
    }

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.trashRecycler.layoutManager?.onSaveInstanceState()
    }


}