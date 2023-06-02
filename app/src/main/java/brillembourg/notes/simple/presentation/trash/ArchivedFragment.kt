package brillembourg.notes.simple.presentation.trash

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentTrashBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.custom_views.animateWithRecycler
import brillembourg.notes.simple.presentation.custom_views.copy
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.custom_views.shareText
import brillembourg.notes.simple.presentation.detail.setupExtrasToDetail
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionState
import brillembourg.notes.simple.presentation.home.renderers.LayoutChangeRenderer
import brillembourg.notes.simple.presentation.home.renderers.NoteUiRenderer
import brillembourg.notes.simple.presentation.home.renderers.SelectionRenderer
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.SearchManager
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.toLayoutType
import brillembourg.notes.simple.presentation.ui_utils.setTransitionToEditNote
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

    private val noteRenderer by lazy {
        NoteUiRenderer(
            binding.trashRecycler,
            recylerViewState,
            onLayoutType = { viewModel.archivedUiState.value.noteLayout.toLayoutType() },
            onNavigateToCategories = {},
            onSelection = { isSelected: Boolean, id: Long -> viewModel.onSelection() },
            onNoteClick = { viewModel.onNoteClick(it) },
            onReorderedNotes = {},
            onReorderedNotesCancelled = {}
        )
    }

    private val changeLayoutRenderer by lazy {
        LayoutChangeRenderer(
            binding.trashRecycler,
            onLayoutChange = { viewModel.onLayoutChange(it) }
        )
    }

    private val selectionRenderer by lazy {
        SelectionRenderer(
            toolbar = requireActivity().findViewById(R.id.toolbar),
            menuId = R.menu.menu_contextual_trash,
            recyclerView = binding.trashRecycler,
            onSelectionDismissed = { viewModel.onSelectionDismissed() },
            onActionClick = {
                when (it) {
                    R.id.menu_context_menu_delete -> {
                        viewModel.noteDeletionManager.onDeleteConfirm()
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
                    R.id.menu_context_copy -> {
                        viewModel.onCopy()
                        true
                    }

                    else -> false
                }
            }
        )
    }

    private val searchManager by lazy {
        val toolbarMain: Toolbar = requireActivity().findViewById(R.id.toolbar)
        SearchManager(
            fragment = this,
            toolbar = toolbarMain,
            onSearch = viewModel::onSearch,
            onDestroyActionMode = {})
    }

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
        menu.findItem(R.id.menu_home_categories).isVisible = false
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val menuHost = requireActivity()
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                changeLayoutRenderer.onClickVerticalLayout()
                menuHost.invalidateMenu()
                return true
            }

            R.id.menu_home_staggered -> {
                changeLayoutRenderer.onClickStaggeredLayout()
                menuHost.invalidateMenu()
                return true
            }

            R.id.menu_home_search -> {
                search()
            }
        }
        return false
    }

    private fun search() {
        searchManager.startSearch()
    }

    override fun onDestroyView() {
        noteRenderer.saveRecyclerState()
        super.onDestroyView()
    }

    private fun renderStates() {

        safeUiLaunch {
            viewModel.navigates.collect { navigates ->
                when (navigates) {
                    is ArchivedUiNavigates.NavigateToEditNote -> navigateToDetailState(navigates)
                    ArchivedUiNavigates.Idle -> Unit
                }
            }
        }

        safeUiLaunch {
            viewModel.archivedUiState.collect { uiState: ArchivedUiState ->

                noteRenderer.render(uiState.noteList)

                selectionRenderer.render(uiState.selectionModeActive)

                changeLayoutRenderer.render(uiState.noteLayout)

                copyClipboardState(uiState.noteActions.copyToClipboard)

                shareNotesAsStringState(uiState.noteActions.shareNoteAsString)

                emptyNotesState(uiState.emptyNote)
            }
        }

        safeUiLaunch {
            viewModel.noteDeletionManager.dialogs.collect { dialogState ->
                when (dialogState) {
                    is NoteDeletionState.ConfirmDeleteDialog -> {
                        showDeleteConfirmationState(dialogState)
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun emptyNotesState(emptyNotesState: ArchivedViewModel.EmptyNote) {
        when (emptyNotesState) {
            ArchivedViewModel.EmptyNote.NoArchived -> {
                binding.trashTextEmpty.setText(R.string.trash_empty)
                binding.trashTextEmpty.isVisible = true
            }

            ArchivedViewModel.EmptyNote.EmptyForSearch -> {
                binding.trashTextEmpty.setText(R.string.search_no_notes_found)
                binding.trashTextEmpty.isVisible = true
            }

            ArchivedViewModel.EmptyNote.None -> {
                binding.trashTextEmpty.isVisible = false
                binding.trashTextEmpty.text = ""
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

    private fun getConcatAdapter() = (binding.trashRecycler.adapter as? ConcatAdapter?)

    private fun showDeleteConfirmationState(showDeleteConfirmationState: NoteDeletionState.ConfirmDeleteDialog?) {
        showDeleteConfirmationState?.let {
            showDeleteTasksDialog(
                fragment = this,
                size = showDeleteConfirmationState.tasksToArchiveSize,
                onPositive = {
                    onDeleteNotes()
                },
                onDismiss = {
                    viewModel.noteDeletionManager.onDismissConfirm()
                })
        }
    }

    private fun navigateToDetailState(navigateToDetail: ArchivedUiNavigates.NavigateToEditNote) {
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

    private fun onShareNotes() {
        viewModel.onShare()
    }

    private fun onDeleteNotes() {
        viewModel.noteDeletionManager.onDeleteNotes()
    }

    private fun onUnarchiveTasks() {
        viewModel.noteDeletionManager.onUnarchiveTasks()
    }

}