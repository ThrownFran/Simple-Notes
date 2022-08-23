package brillembourg.notes.simple.presentation.home

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
import brillembourg.notes.simple.databinding.FragmentHomeBinding
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.custom_views.*
import brillembourg.notes.simple.presentation.detail.setupExtrasToDetail
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.getNoteSelectedTitle
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.*
import brillembourg.notes.simple.presentation.ui_utils.setupContextualActionBar
import brillembourg.notes.simple.presentation.ui_utils.showArchiveConfirmationDialog
import brillembourg.notes.simple.presentation.ui_utils.showDeleteTasksDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding: FragmentHomeBinding

    private var recyclerViewState: Parcelable? = null
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = _binding as FragmentHomeBinding
        binding.viewmodel = viewModel
        setupListeners()
        setupMenu()
        renderStates()
        animateFabWithRecycler()
        return binding.root
    }

    private fun setupListeners() {
        val activityBinding = (activity as MainActivity?)?.binding

        safeUiLaunch {
            activityBinding?.homeFab?.onClickFlow?.collect {
                viewModel.onAddNoteClick()
            }
        }
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
        val layoutType = viewModel.homeUiState.value.noteLayout.toLayoutType()
        menu.findItem(R.id.menu_home_vertical)
            .apply { isVisible = layoutType == LayoutType.Staggered }
        menu.findItem(R.id.menu_home_staggered)
            .apply { isVisible = layoutType == LayoutType.LinearVertical }
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
        val noteAdapter = recyclerView.adapter as NoteAdapter? ?: return

        changeLayout(
            recyclerView,
            layoutType,
            noteAdapter.currentList
        )

        noteAdapter.changeDragDirections(recyclerView, getDragDirs(layoutType))
    }

    private fun clickStaggeredLayout() {
        viewModel.onLayoutChange(NoteLayout.Grid)
    }

    private fun clickVerticalLayout() {
        viewModel.onLayoutChange(NoteLayout.Vertical)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        saveRecyclerState()
//        super.onSaveInstanceState(outState)
//    }

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun renderStates() {

        safeUiLaunch {
            viewModel.homeUiState.collect { homeUiState: HomeUiState ->

                setupNoteState(homeUiState.noteList)

                selectionModeState(homeUiState.selectionModeActive)

                navigateToDetailState(homeUiState.navigateToEditNote)

                navigateToAddNoteState(homeUiState.navigateToAddNote)

                showArchiveConfirmationState(homeUiState.showArchiveNotesConfirmation)

                showDeleteConfirmationState(homeUiState.showDeleteNotesConfirmation)

                noteListLayoutState(homeUiState.noteLayout)

                copyClipboardState(homeUiState.copyToClipboard)

                shareNotesAsStringState(homeUiState.shareNoteAsString)
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


    private fun noteListLayoutState(noteLayout: NoteLayout) {
        clickChangeLayout(
            binding.homeRecycler,
            noteLayout.toLayoutType()
        )
    }

    private fun showDeleteConfirmationState(showDeleteConfirmationState: ShowDeleteNotesConfirmationState?) {
        if (showDeleteConfirmationState != null) {
            showDeleteTasksDialog(this, showDeleteConfirmationState.tasksToDeleteSize,
                onPositive = {
                    viewModel.onDeleteNotes()
                },
                onDismiss = {
                    viewModel.onDismissConfirmDeleteShown()
                })
        }
    }

    private fun showArchiveConfirmationState(showArchiveConfirmationState: ShowArchiveNotesConfirmationState?) {
        if (showArchiveConfirmationState != null) {
            showArchiveConfirmationDialog(this, showArchiveConfirmationState.tasksToArchiveSize,
                onPositive = {
                    viewModel.onArchiveNotes()
                },
                onDismiss = {
                    viewModel.onDismissConfirmArchiveShown()
                })
        }
    }

    private fun navigateToAddNoteState(navigateToAddNote: Boolean) {
        if (navigateToAddNote) {
            navigateToCreateTask()
            viewModel.onNavigateToAddNoteCompleted()
        }
    }

    private fun selectionModeState(selectionModeActive: SelectionModeActive?) {
        if (selectionModeActive == null) {
            actionMode?.finish()
            actionMode = null
            return
        }
        launchContextualActionBar(selectionModeActive.size)
    }


    private fun navigateToDetailState(navigateToDetail: NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val view =
                binding.homeRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!!)!!.itemView
            navigateToDetail(navigateToDetail.notePresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun navigateToCreateTask() {
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        setTransitionToCreateNote()
        findNavController().navigate(directions)
    }

    private fun navigateToDetail(it: NotePresentationModel, view: View) {
        //navigate to detail fragment
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.task = it

        setTransitionToEditNote()
        findNavController().navigate(directions, setupExtrasToDetail(view))
    }

    private fun setupNoteState(noteList: NoteList) {
        if (noteList.mustRender) setupNoteList(noteList.notes)
    }

    private fun setupNoteList(taskList: List<NotePresentationModel>) {
        if (binding.homeRecycler.adapter == null) {
            setupTaskRecycler(taskList)
        } else {
            updateListAndNotify(binding.homeRecycler.adapter as NoteAdapter, taskList)
        }
    }

    private fun setupTaskRecycler(taskList: List<NotePresentationModel>) {
        binding.homeRecycler.apply {
            val layoutType = viewModel.homeUiState.value.noteLayout.toLayoutType()
            adapter = buildTaskAdapter(this, taskList, getDragDirs(layoutType))
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//            adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    private fun updateListAndNotify(
        noteAdapter: NoteAdapter,
        taskList: List<NotePresentationModel>
    ) {
        submitListAndScrollIfApplies(noteAdapter, noteAdapter.currentList, taskList)
    }

    private fun submitListAndScrollIfApplies(
        noteAdapter: NoteAdapter,
        currentList: List<NotePresentationModel>,
        taskList: List<NotePresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        noteAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
    }

    private fun scrollToTop() {
        binding.homeRecycler.scrollToPosition(0)
    }

    private fun buildTaskAdapter(
        recyclerView: RecyclerView,
        taskList: List<NotePresentationModel>,
        dragDirs: Int
    ): NoteAdapter {

        return NoteAdapter(
            dragDirs,
            recyclerView,
            onSelection = {
                onNoteSelection()
            },
            onClick = { task ->
                onNoteClicked(task)
            },
            onReorderSuccess = { tasks ->
                onReorderedNotes(tasks)
            },
            onReorderCanceled = {
                onReorderNotesCancelled()
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
            menuId = R.menu.menu_contextual_home,
            currentActionMode = actionMode,
            adapter = binding.homeRecycler.adapter as NoteAdapter,
            onActionClick = { onContextualActionItem(menuId = it) },
            onSetTitle = { selectedSize: Int ->
                getNoteSelectedTitle(
                    resources = resources,
                    selectedSize = selectedSize
                )
            },
            onDestroyMyActionMode = {
                viewModel.onSelectionDismissed()
            }
        )
    }

    private fun onContextualActionItem(menuId: Int) = when (menuId) {
        R.id.menu_context_menu_archive -> {
            onArchiveTasks()
            true
        }
        R.id.menu_context_menu_delete -> {
            onDeleteTasks()
            true
        }

        R.id.menu_context_copy -> {
            onCopyNotes()
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

    private fun onCopyNotes() {
        viewModel.onCopy()
    }

    private fun onDeleteTasks() {
        viewModel.onDeleteConfirm()
    }

    private fun onReorderedNotes(tasks: List<NotePresentationModel>) {
        viewModel.onReorderedNotes(tasks)
    }

    private fun onReorderNotesCancelled() {
        viewModel.onSelectionDismissed()
    }

    private fun onNoteClicked(it: NotePresentationModel) {
        viewModel.onNoteClick(it)
    }

    private fun onArchiveTasks() {
        viewModel.onArchiveConfirmNotes()
    }

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recyclerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recyclerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}