package brillembourg.notes.simple.presentation.home

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
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentHomeBinding
import brillembourg.notes.simple.domain.models.NoteLayout
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.extras.*
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.ui_utils.*
import brillembourg.notes.simple.util.UiText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    //    private var confirmationArchiveDialog: AlertDialog? = null
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var binding: FragmentHomeBinding

    private var recylerViewState: Parcelable? = null
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
        setupObservers()
        animateFabWithRecycler()
        return binding.root
    }

    private fun setupListeners() {
        val activityBinding = (activity as MainActivity?)?.binding
        activityBinding?.homeFab?.setOnClickListener {
            viewModel.onAddNoteClick()
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
        viewModel.onLayoutChange(NoteLayout.Grid)
    }

    private fun clickVerticalLayout() {
        viewModel.onLayoutChange(NoteLayout.Vertical)
    }

    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeUiState.collect { homeUiState: HomeUiState ->
                    selectionModeObserver(homeUiState.selectionModeState)
                    userMessageObserver(homeUiState.userMessage)
                    navigateToDetailObserver(homeUiState.navigateToEditNote)
                    navigateToAddNoteObserver(homeUiState.navigateToAddNote)
                    showArchiveConfirmationObserver(homeUiState.showArchiveNotesConfirmation)
                    noteLayoutPreferenceObserver(homeUiState.noteLayout)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.taskListState.collect {
                    setupTaskList(it)
                }
            }
        }


    }

    private fun noteLayoutPreferenceObserver(noteLayout: NoteLayout) {
        clickChangeLayout(
            binding.homeRecycler,
            noteLayout.toLayoutType()
        )
    }

    private fun showArchiveConfirmationObserver(showArchiveConfirmationState: ShowArchiveNotesConfirmationState) {
        if (showArchiveConfirmationState.isVisible) {
            showArchiveConfirmationDialog(showArchiveConfirmationState.tasksToArchiveSize) {
                viewModel.onDismissConfirmArchiveShown()
            }
        }
    }

    private fun navigateToAddNoteObserver(navigateToAddNote: Boolean) {
        if (navigateToAddNote) {
            navigateToCreateTask()
            viewModel.onNavigateToAddNoteCompleted()
        }
    }

    private fun selectionModeObserver(selectionModeState: SelectionModeState) {
        if (!selectionModeState.isActive) {
            actionMode?.finish()
            actionMode = null
            return
        }
        launchContextualActionBar(selectionModeState.size)
    }

    private fun userMessageObserver(userMessage: UiText?) {
        userMessage?.let {
            showMessage(it) {
                viewModel.onMessageShown()
            }
        }
    }

    private fun navigateToDetailObserver(navigateToDetail: NavigateToEditNote) {
        if (navigateToDetail.mustConsume) {
            val view =
                binding.homeRecycler.findViewHolderForAdapterPosition(navigateToDetail.taskIndex!!)!!.itemView
            navigateToDetail(navigateToDetail.taskPresentationModel!!, view)
            viewModel.onNavigateToDetailCompleted()
        }
    }

    private fun navigateToCreateTask() {
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        setTransitionToCreateNote()
        findNavController().navigate(directions)
    }

    private fun navigateToDetail(it: TaskPresentationModel, view: View) {
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
            val layoutType = viewModel.homeUiState.value.noteLayout.toLayoutType()
            adapter = buildTaskAdapter(this, taskList, getDragDirs(layoutType))
            layoutManager = buildLayoutManager(context, layoutType).also { layoutManager ->
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
                onNoteSelection()
            },
            onClick = { task, _ ->
                onNoteClicked(task)
            },
            onReorderSuccess = { tasks ->
                onReorderedNotes(tasks)
            },
            onReorderCanceled = {
                onReorderNotesCancelled()
            })
            .also {
                it.submitList(taskList)
                it.itemTouchHelper.attachToRecyclerView(recyclerView)
            }
    }


    private fun onNoteSelection() {
        viewModel.onSelection()
    }

    private fun launchContextualActionBar(sizeSelected: Int) {
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
            onDestroyMyActionMode = {
                viewModel.onSelectionDismissed()
            }
        )
    }

    private fun onContextualActionItem(menuId: Int) = when (menuId) {
        R.id.menu_context_menu_delete -> {
            clickArchiveTasks()
            true
        }
        else -> false
    }

    private fun onReorderedNotes(tasks: List<TaskPresentationModel>) {
        viewModel.onReorderedNotes(tasks)
    }

    private fun onReorderNotesCancelled() {
        viewModel.onSelectionDismissed()
    }

    private fun onNoteClicked(it: TaskPresentationModel) {
        viewModel.onNoteClick(it)
    }

    private fun clickArchiveTasks() {
        viewModel.onShowConfirmArchiveNotes()
    }

    private fun showArchiveConfirmationDialog(
        size: Int,
        onDismiss: () -> Unit
    ) {
        val title =
            if (size > 1) getString(R.string.move_tasks_to_trash) else getString(R.string.move_task_to_trash)

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_delete_dark_24)
            //            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.all_cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.all_move_to_trash)) { dialog, which ->
                viewModel.onArchiveNotes()
            }
            .setOnDismissListener {
                onDismiss.invoke()
            }
            .showWithLifecycle(viewLifecycleOwner)
    }


    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}