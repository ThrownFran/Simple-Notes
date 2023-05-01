package brillembourg.notes.simple.presentation.home

import android.os.Bundle
import android.os.Parcelable
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.categories.toDiplayOrder
import brillembourg.notes.simple.presentation.custom_views.animateWithRecycler
import brillembourg.notes.simple.presentation.custom_views.copy
import brillembourg.notes.simple.presentation.custom_views.onClickFlow
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.custom_views.setTransitionToCreateNote
import brillembourg.notes.simple.presentation.custom_views.setTransitionToEditNote
import brillembourg.notes.simple.presentation.custom_views.shareText
import brillembourg.notes.simple.presentation.detail.setupExtrasToDetail
import brillembourg.notes.simple.presentation.home.delete.NoteDeletionState
import brillembourg.notes.simple.presentation.home.renderers.LayoutChangeRenderer
import brillembourg.notes.simple.presentation.home.renderers.NoteUiRenderer
import brillembourg.notes.simple.presentation.home.renderers.SelectionRenderer
import brillembourg.notes.simple.presentation.models.NotePresentationModel
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.LayoutType
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.toLayoutType
import brillembourg.notes.simple.presentation.ui_utils.showArchiveConfirmationDialog
import brillembourg.notes.simple.presentation.ui_utils.showDeleteTasksDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private var actionMode: ActionMode? = null
    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding: FragmentHomeBinding

    private var recyclerViewState: Parcelable? = null

    private val noteRenderer by lazy {
        NoteUiRenderer(
            binding.homeRecycler,
            recyclerViewState,
            onLayoutType = { viewModel.homeUiState.value.noteLayout.toLayoutType() },
            onNavigateToCategories = { viewModel.onNavigateToCategories() },
            onSelection = { viewModel.onSelection() },
            onNoteClick = { viewModel.onNoteClick(it) },
            onReorderedNotes = { viewModel.onReorderedNotes(it) },
            onReorderedNotesCancelled = { viewModel.onReorderNotesCancelled() },
            onWizardVisibility = { binding.homeWizard.isVisible = it }
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
            viewModel.noteList.collect {
                noteRenderer.render(it)
            }
        }

        safeUiLaunch {
            viewModel.homeUiState.collect { homeUiState: HomeUiState ->

                selectionRenderer.render(homeUiState.selectionModeActive)

                changeLayoutRenderer.render(homeUiState.noteLayout)

                copyClipboardState(homeUiState.noteActions.copyToClipboard)

                shareNotesAsStringState(homeUiState.noteActions.shareNoteAsString)

                selectCategoriesState(homeUiState.selectCategoriesState)
            }
        }

        safeUiLaunch {
            viewModel.noteDeletionManager.dialogs.collect { dialogState ->
                when (dialogState) {
                    is NoteDeletionState.ConfirmArchiveDialog -> {
                        showDeleteConfirmationState(dialogState)
                    }

                    is NoteDeletionState.ConfirmDeleteDialog -> {
                        showArchiveConfirmationState(dialogState)
                    }

                    else -> Unit
                }
            }
        }

        safeUiLaunch {
            viewModel.filteredCategories.collectLatest {
                filteredCategoriesState(it)
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


    //region Categories

    private fun filteredCategoriesState(filteredCategories: List<CategoryPresentationModel>) {

        val headerAdapter = getHeaderAdapter()

        if (filteredCategories.isEmpty()) {
            headerAdapter?.let { getConcatAdapter()?.removeAdapter(it) }
            return
        }

        if (headerAdapter?.filteredCategories?.size == filteredCategories.size) return

        if (headerAdapter != null) {
            headerAdapter.filteredCategories.clear()
            headerAdapter.filteredCategories.addAll(filteredCategories)
            headerAdapter.notifyItemChanged(0, Any())
        } else {
            (binding.homeRecycler.adapter as? ConcatAdapter?)?.addAdapter(
                0,
                HeaderAdapter(filteredCategories.toDiplayOrder().toMutableList()) {
                    viewModel.onNavigateToCategories()
                })
        }
    }

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
                changeLayoutRenderer.onClickVerticalLayout()
                menuHost.invalidateMenu()
                return true
            }
            R.id.menu_home_staggered -> {
                changeLayoutRenderer.onClickStaggeredLayout()
                menuHost.invalidateMenu()
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
        val toolbarMain: Toolbar = requireActivity().findViewById(R.id.toolbar)

        actionMode = toolbarMain.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {

                val customView = LayoutInflater.from(context).inflate(R.layout.layout_search, null)
                mode.customView = customView
                val searchView = customView.findViewById<SearchView>(R.id.searchView)

                val searchText =
                    searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                searchText.setTextColor(
                    ContextCompat.getColor(
                        searchView.context, R.color
                            .md_theme_light_onPrimary
                    )
                )
                searchText.setHintTextColor(
                    ContextCompat.getColor(
                        searchView.context, R.color
                            .md_theme_light_secondaryContainer_transparent
                    )
                )
                searchView.isIconified = false
                searchView.requestFocus()
                searchView.queryHint = "Search note ..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        // perform search
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        // update search suggestions
                        viewModel.onSearch(newText)
                        return true
                    }
                })
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                // customize action mode menu
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                actionMode = null
            }
        })
    }

    //endregion

    //region Navigation

    private fun navigateToAddNoteState(navigateToAddNote: HomeUiNavigates.NavigateToAddNote) {
        navigateToAddNote.let {
            navigateToCreateTask(navigateToAddNote.content)
            viewModel.onNavigateToAddNoteCompleted()
        }
    }

    private fun navigateToDetailState(navigateToDetail: HomeUiNavigates.NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val headers = if (getHeaderAdapter() == null) 0 else 1
            val view =
                binding.homeRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!! + headers)!!.itemView
            navigateToDetail(navigateToDetail.notePresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
            runBlocking { }
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

    private fun showDeleteConfirmationState(showDeleteConfirmationState: NoteDeletionState.ConfirmArchiveDialog) {
        showDeleteTasksDialog(this, showDeleteConfirmationState.tasksToDeleteSize,
            onPositive = {
                viewModel.noteDeletionManager.onDeleteNotes()
            },
            onDismiss = {
                viewModel.noteDeletionManager.onDismissConfirmDeleteShown()
            })
    }

    private fun onDeleteNotesConfirm() {
        viewModel.noteDeletionManager.onDeleteConfirm()
    }

    //endregion

    //region Archive

    private fun showArchiveConfirmationState(showArchiveConfirmationState: NoteDeletionState.ConfirmDeleteDialog) {
        showArchiveConfirmationDialog(this, showArchiveConfirmationState.tasksToArchiveSize,
            onPositive = {
                viewModel.noteDeletionManager.onArchiveNotes()
            },
            onDismiss = {
                viewModel.noteDeletionManager.onDismissConfirm()
            })
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