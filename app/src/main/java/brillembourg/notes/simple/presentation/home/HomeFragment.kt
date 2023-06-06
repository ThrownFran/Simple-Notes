package brillembourg.notes.simple.presentation.home

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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentHomeBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.base.MainViewModel
import brillembourg.notes.simple.presentation.custom_views.animateWithRecycler
import brillembourg.notes.simple.presentation.custom_views.copy
import brillembourg.notes.simple.presentation.custom_views.onClickFlow
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
import brillembourg.notes.simple.presentation.ui_utils.setTransitionToCreateNote
import brillembourg.notes.simple.presentation.ui_utils.setTransitionToEditNote
import brillembourg.notes.simple.presentation.ui_utils.showArchiveConfirmationDialog
import brillembourg.notes.simple.presentation.ui_utils.showDeleteTasksDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding: FragmentHomeBinding

    private var recyclerViewState: Parcelable? = null

    private var menu: Menu? = null

    private val noteRenderer by lazy {
        NoteUiRenderer(
            binding.homeRecycler,
            recyclerViewState,
            onLayoutType = viewModel.homeUiState.value.noteLayout::toLayoutType,
            onNavigateToCategories = viewModel::onNavigateToCategories,
            onSelection = viewModel::onSelection,
            onNoteClick = viewModel::onNoteClick,
            onReorderedNotes = viewModel::onReorderedNotes,
            onReorderedNotesCancelled = viewModel::onReorderNotesCancelled
        )
    }

    private val selectionRenderer by lazy {
        SelectionRenderer(
            toolbar = requireActivity().findViewById(R.id.toolbar),
            menuId = R.menu.menu_contextual_home,
            recyclerView = binding.homeRecycler,
            onSelectionDismissed = { viewModel.onSelectionDismissed() },
            onActionClick = { menuId ->
                when (menuId) {
                    R.id.menu_context_menu_archive -> {
                        onArchiveTasks()
                        true
                    }

                    R.id.menu_context_menu_delete -> {
                        onDeleteNotesConfirm()
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
            }
        )
    }

    private val changeLayoutRenderer by lazy {
        LayoutChangeRenderer(
            binding.homeRecycler,
            onLayoutChange = { viewModel.onLayoutChange(it) }
        )
    }

    private val searchManager by lazy {
        val toolbarMain: Toolbar = requireActivity().findViewById(R.id.toolbar)
        SearchManager(
            fragment = this,
            toolbar = toolbarMain,
            onSearch = viewModel::onSearch,
            onDestroyActionMode = viewModel::onSearchCancelled
        )
    }

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


    private fun renderStates() {

        safeUiLaunch {
            viewModel.navigates.collect { navigates ->
                when (navigates) {
                    is HomeUiNavigates.NavigateToAddNote -> navigateToAddNoteState(navigates)
                    is HomeUiNavigates.NavigateToEditNote -> navigateToDetailState(navigates)
                    HomeUiNavigates.Idle -> Unit
                }
            }
        }

        safeUiLaunch {
            viewModel.selectCategoriesState.collect { state ->
                selectCategoriesState(state)
            }
        }

        safeUiLaunch {
            viewModel.homeUiState.collect { homeUiState: HomeUiState ->

                binding.homeProgress.isVisible = homeUiState.isLoading

                noteRenderer.render(homeUiState.noteList)

                selectionRenderer.render(homeUiState.selectionModeActive)

                changeLayoutRenderer.render(homeUiState.noteLayout)

                copyClipboardState(homeUiState.noteActions.copyToClipboard)

                shareNotesAsStringState(homeUiState.noteActions.shareNoteAsString)

                emptyNotesState(homeUiState.emptyNotesState)

                searchManager.onCheckState(homeUiState.noteList.key)

                updateMenu(
                    menu = menu,
                    layoutType = homeUiState.noteLayout.toLayoutType()
                )
            }
        }

        safeUiLaunch {
            viewModel.noteDeletionManager.dialogs.collect { dialogState ->
                when (dialogState) {
                    is NoteDeletionState.ConfirmDeleteDialog -> {
                        showDeleteConfirmationState(dialogState)
                    }

                    is NoteDeletionState.ConfirmArchiveDialog -> {
                        showArchiveConfirmationState(dialogState)
                    }

                    else -> Unit
                }
            }
        }

        safeUiLaunch {
            activityViewModel.incomingContentFromExternalApp.collect { content ->
                content?.let {
                    viewModel.onAddNoteClick(it)
                    activityViewModel.onIncommingContentProcessed()
                }
            }
        }
    }

    private fun emptyNotesState(emptyNotesState: HomeUiState.EmptyNote) {
        when (emptyNotesState) {
            HomeUiState.EmptyNote.Wizard -> {
                binding.homeWizardText.setText(R.string.wizard_text)
                binding.homeWizard.isVisible = true
            }

            HomeUiState.EmptyNote.EmptyForLabel -> {
                binding.homeWizardText.setText(R.string.label_empty_text)
                binding.homeWizard.isVisible = true
            }

            HomeUiState.EmptyNote.EmptyForSearch -> {
                binding.homeWizardText.setText(R.string.search_no_notes_found)
                binding.homeWizard.isVisible = true
            }

            HomeUiState.EmptyNote.EmptyForMultipleLabels -> {
                binding.homeWizardText.setText(R.string.labels_empty_text)
                binding.homeWizard.isVisible = true
            }

            HomeUiState.EmptyNote.None -> {
                binding.homeWizard.isVisible = false
                binding.homeWizardText.text = ""
            }
        }
    }


    //region Categories

    private fun selectCategoriesState(selectCategories: SelectCategoriesState) {
        if (selectCategories.navigate && !selectCategories.isShowing) {
            showCategoriesModalBottomSheet()
            viewModel.onCategoriesShowing()
        }
    }

    private fun showCategoriesModalBottomSheet() {
        val selectCategoriesModalBottomSheet = SelectHomeFilterCategoriesModal()
        selectCategoriesModalBottomSheet.show(
            childFragmentManager,
            SelectHomeFilterCategoriesModal.TAG
        )
    }

    //endregion

    //region Menu

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        this.menu = menu
        updateMenu(menu, viewModel.homeUiState.value.noteLayout.toLayoutType())
    }

    private fun updateMenu(menu: Menu?, layoutType: LayoutType) {
        menu?.apply {
            findItem(R.id.menu_home_vertical)?.apply {
                isVisible = layoutType == LayoutType.Staggered
            }
            findItem(R.id.menu_home_staggered)?.apply {
                isVisible = layoutType == LayoutType.LinearVertical
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                changeLayoutRenderer.onClickVerticalLayout()
                return true
            }

            R.id.menu_home_staggered -> {
                changeLayoutRenderer.onClickStaggeredLayout()
                return true
            }

            R.id.menu_home_categories -> {
                viewModel.onNavigateToCategories()
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

    //endregion

    //region Navigation

    private fun navigateToAddNoteState(navigateToAddNote: HomeUiNavigates.NavigateToAddNote) {
        navigateToAddNote.let {
            navigateToCreateTask(navigateToAddNote.content)
            searchManager.actionMode?.finish()
            viewModel.onNavigateToAddNoteCompleted()
        }
    }

    private fun navigateToDetailState(navigateToDetail: HomeUiNavigates.NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val headers = if (getHeaderAdapter() == null) 0 else 1
            val view =
                binding.homeRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!! + headers)!!.itemView
            navigateToDetail(navigateToDetail.notePresentationModel!!, view)
            searchManager.actionMode?.finish()
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun getHeaderAdapter() =
        getConcatAdapter()?.adapters?.filterIsInstance<HeaderAdapter>()?.firstOrNull()

    private fun getConcatAdapter() = (binding.homeRecycler.adapter as? ConcatAdapter?)

    private fun navigateToCreateTask(content: String? = null) {
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.contentOptional = content
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

    //endregion

    //region Delete

    private fun showDeleteConfirmationState(showDeleteConfirmationState: NoteDeletionState.ConfirmDeleteDialog) {
        showDeleteTasksDialog(
            this, showDeleteConfirmationState.tasksToDeleteSize,
            onPositive = viewModel.noteDeletionManager::onDeleteNotes,
            onDismiss = viewModel.noteDeletionManager::onDismissConfirmDeleteShown
        )
    }

    private fun onDeleteNotesConfirm() {
        viewModel.noteDeletionManager.onDeleteConfirm()
    }

    //endregion

    //region Archive

    private fun showArchiveConfirmationState(showArchiveConfirmationState: NoteDeletionState.ConfirmArchiveDialog) {
        showArchiveConfirmationDialog(
            this, showArchiveConfirmationState.tasksToArchiveSize,
            onPositive = viewModel.noteDeletionManager::onArchiveNotes,
            onDismiss = viewModel.noteDeletionManager::onDismissConfirm
        )
    }

    private fun onArchiveTasks() {
        viewModel.noteDeletionManager.onArchiveConfirmNotes()
    }

    //endregion

    //region Share

    private fun shareNotesAsStringState(shareNoteAsString: String?) {
        shareNoteAsString?.let {
            shareText(shareNoteAsString)
            viewModel.onShareCompleted()
        }
    }

    private fun onShareNotes() {
        viewModel.onShare()
    }

    //endregion

    //region Copy

    private fun copyClipboardState(copyToClipboard: String?) {
        copyToClipboard?.let {
            copy(it)
            viewModel.onCopiedCompleted()
        }
    }

    private fun onCopyNotes() {
        viewModel.onCopy()
    }

    //endregion

    private fun setupListeners() {
        val activityBinding = (activity as MainActivity?)?.binding

        safeUiLaunch {
            activityBinding?.homeFab?.onClickFlow?.collect {
                viewModel.onAddNoteClick()
            }
        }

        safeUiLaunch {
            binding.homeWizard.onClickFlow.collect {
                viewModel.onAddNoteClick()
            }
        }
    }

    private fun animateFabWithRecycler() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.homeFab?.animateWithRecycler(binding.homeRecycler)
    }

    override fun onDestroyView() {
        noteRenderer.saveRecyclerState()
        super.onDestroyView()
    }
}