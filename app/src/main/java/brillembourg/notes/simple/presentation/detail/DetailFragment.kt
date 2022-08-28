package brillembourg.notes.simple.presentation.detail

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentDetailBinding
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.custom_views.*
import brillembourg.notes.simple.presentation.ui_utils.showArchiveConfirmationDialog
import brillembourg.notes.simple.presentation.ui_utils.showDeleteTasksDialog
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


@AndroidEntryPoint
class DetailFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private lateinit var binding: FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEditNoteEnteringTransition()
        setupBackPhysicalButtonListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        setHasOptionsMenu(true)
        unfocusScreenWhenKeyboardHidden()
        setupMenu()
        return binding.root
    }


    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_note, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val isNewTask = viewModel.uiDetailUiState.value.isNewTask
        val isArchived = viewModel.uiDetailUiState.value.isArchivedTask

        menuUnarchiveVisibility(menu, isArchived, isNewTask)
        menuArchiveVisibility(menu, isArchived, isNewTask)
        menuDeleteVisibilityAndOptions(menu, isNewTask, isArchived)
        menuShareAndCopyVisibility(menu)
    }

    private fun menuShareAndCopyVisibility(menu: Menu) {
        val userInput = viewModel.uiDetailUiState.value.userInput
        val isShareOrCopyEnabled = !userInput.isNullOrEmpty()
        menu.findItem(R.id.menu_note_share).apply {
            isVisible = isShareOrCopyEnabled
        }
        menu.findItem(R.id.menu_note_copy).apply {
            isVisible = isShareOrCopyEnabled
        }
    }

    private fun menuDeleteVisibilityAndOptions(
        menu: Menu,
        isNewTask: Boolean,
        isArchived: Boolean
    ) {
        menu.findItem(R.id.menu_note_delete).apply {
            isVisible = !isNewTask
            setShowAsAction(
                if (isArchived && !isNewTask) {
                    MenuItem.SHOW_AS_ACTION_ALWAYS
                } else {
                    MenuItem.SHOW_AS_ACTION_NEVER
                }
            )
        }
    }

    private fun menuArchiveVisibility(
        menu: Menu,
        isArchived: Boolean,
        isNewTask: Boolean
    ) {
        menu.findItem(R.id.menu_note_archive).apply {
            isVisible = !isArchived && !isNewTask
        }
    }

    private fun menuUnarchiveVisibility(
        menu: Menu,
        isArchived: Boolean,
        isNewTask: Boolean
    ) {
        menu.findItem(R.id.menu_note_unachive).apply {
            isVisible = isArchived && !isNewTask
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_note_unachive -> {
                viewModel.onUnarchive()
                return true
            }
            R.id.menu_note_delete -> {
                showDeleteTasksDialog(
                    fragment = this,
                    size = 1,
                    onPositive = {
                        viewModel.onDelete()
                    })
                return true
            }

            R.id.menu_note_archive -> {
                showArchiveConfirmationDialog(
                    fragment = this,
                    size = 1,
                    onPositive = { viewModel.onArchive() })
                return true
            }

            R.id.menu_note_copy -> {
                onCopy()
            }

            R.id.menu_note_share -> {
                onShare()
            }

            R.id.menu_note_label -> {
                onAddCategories()
            }
        }
        return false
    }

    private fun onAddCategories() {
        viewModel.onNavigateToCategories()
    }

    private fun onShare() {
        val textToCopy = generateTextToCopy()
        shareText(textToCopy)
    }


    private fun generateTextToCopy(): String {
        val title = viewModel.uiDetailUiState.value.userInput.title
        val content = viewModel.uiDetailUiState.value.userInput.content

        return StringBuilder(title)
            .append((if (title.isNotEmpty()) "\n\n" else ""))
            .append(content)
            .toString()
    }

    private fun onCopy() {
        copy(generateTextToCopy())
    }

    private fun unfocusScreenWhenKeyboardHidden() {
        setEventListener(
            requireActivity(),
            KeyboardVisibilityEventListener {
                // Ah... at last. do your thing :)
                if (!it) {
                    binding.detailEditTitle.clearFocus()
                    binding.detailEditContent.clearFocus()
                    binding.detailLinear.clearFocus()
                }
            })
    }

    private fun clickBack() {
        viewModel.onBackPressed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareTransition(view)
        setCreateNoteEnterTransition(
            myStartView = requireActivity().findViewById(R.id.home_fab),
            myEndView = binding.detailLinear
        )
        renderState()

    }

    private fun showCategoriesModalBottomSheet() {
        val selectCategoriesModalBottomSheet = SelectCategoriesModalBottomSheet()
        selectCategoriesModalBottomSheet.show(
            childFragmentManager,
            SelectCategoriesModalBottomSheet.TAG
        )
    }

    private fun renderState() {
        safeUiLaunch {
            viewModel.uiDetailUiState.collect { uiState ->

                setToolbarTitle(uiState.isNewTask)

                updateToolbarIcons()

                if (uiState.unFocusInput) {
                    unFocus()
                    viewModel.onUnFocusCompleted()
                }

                if (uiState.focusInput) {
                    focus()
                    viewModel.onFocusCompleted()
                }

                if (uiState.navigateBack) {
                    finishView()
                }

                selectCategoriesState(uiState.selectCategories)

                setupCategories(uiState.noteCategories)
            }
        }

        safeUiLaunch {
            viewModel.uiDetailUiState.value.getOnInputChangedFlow()
                .debounce(300)
                .collect {
                    updateToolbarIcons()
                }
        }
    }

    private fun setupCategories(noteCategories: List<CategoryPresentationModel>) {
        binding.detailChipgroup.isVisible = noteCategories.isNotEmpty()

        if (noteCategories.size == binding.detailChipgroup.size) {
            return
        }

        binding.detailChipgroup.removeAllViews()
        noteCategories.forEach { categoryPresentationModel: CategoryPresentationModel ->
            val chip = Chip(context)
            chip.text = categoryPresentationModel.name
            chip.id = categoryPresentationModel.id.toInt()
            binding.detailChipgroup.addView(chip)
            chip.setOnClickListener {
                viewModel.onNavigateToCategories()
//                binding.detailChipgroup.removeView(chip)
//                viewModel.onCategoryChecked(categoryPresentationModel,false)
            }
        }

    }

    private fun selectCategoriesState(selectCategories: SelectCategories) {
        if (selectCategories.navigate && !selectCategories.isShowing) {
            showCategoriesModalBottomSheet()
            viewModel.onCategoriesShowing()
        }
    }

    private fun updateToolbarIcons() {
        val menuHost: MenuHost = requireActivity()
        menuHost.invalidateMenu()
    }

    private fun setToolbarTitle(isNewTask: Boolean) {
        if (isNewTask) {
            //Create or Edit title
            val activityBinding = (activity as MainActivity?)?.binding
            activityBinding?.toolbar?.title = "Add note"
        } else {
            val activityBinding = (activity as MainActivity?)?.binding
            activityBinding?.toolbar?.title = "Edit note"
        }
    }

    private fun focus() {
        binding.detailEditContent.apply {
            requestFocus()
            showSoftKeyboard()
        }
    }

    private fun unFocus() {
        binding.detailLinear.clearFocus()
        binding.detailEditContent.hideKeyboard()
    }

    private fun finishView() {
        findNavController().navigateUp()
//        findNavController().popBackStack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                clickBack()
                return true
            }
        }
        return false
    }

    private fun setupBackPhysicalButtonListener() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                clickBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}

fun setupExtrasToDetail(sharedView: View): FragmentNavigator.Extras {
    return FragmentNavigatorExtras(
        //View from item list
        sharedView.findViewById<View>(R.id.task_roundcontraint)
                //String mapping detail view (transition_name)
                to sharedView.context.getString(R.string.home_shared_detail_container),
    )
}