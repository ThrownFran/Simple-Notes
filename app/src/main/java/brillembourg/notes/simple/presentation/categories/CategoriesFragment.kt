package brillembourg.notes.simple.presentation.categories

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentCategoriesBinding
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.home.delete.HomeDialogsState
import brillembourg.notes.simple.presentation.ui_utils.getCategoriesSelectedTitle
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.buildVerticalManager
import brillembourg.notes.simple.presentation.ui_utils.setupContextualActionBar
import brillembourg.notes.simple.presentation.ui_utils.showDeleteCategoriesDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    companion object {
        fun newInstance() = CategoriesFragment()
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var _binding: FragmentCategoriesBinding? = null
    private lateinit var binding: FragmentCategoriesBinding

    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) _binding =
            FragmentCategoriesBinding.inflate(inflater, container, false)
        binding = _binding as FragmentCategoriesBinding
        binding.viewmodel = viewModel
        renderStates()
        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        binding.categoriesCreateitemview.onReadyToCreateItem = {
            viewModel.onCreateCategory(it)
        }
    }

    private fun renderStates() {
        safeUiLaunch {
            viewModel.categoryUiState.collect {

                setupCategoryList(it.categoryList)

                enableOrDisableCreateCategory(it.createCategory)

                selectionModeState(it.selectionMode)

                showDeleteCategoriesState(it.deleteConfirmation)

            }
        }
    }


    private fun showDeleteCategoriesState(state: HomeDialogsState.DeleteCategoriesConfirmation?) {
        if (state != null) {
            showDeleteCategoriesDialog(this, state.tasksToDeleteSize,
                onPositive = {
                    viewModel.onDeleteCategories()
                },
                onDismiss = {
                    viewModel.onDismissConfirmDeleteShown()
                })
        }
    }

    private fun selectionModeState(selectionMode: SelectionMode?) {
        if (selectionMode == null) {
            actionMode?.finish()
            actionMode = null
            return
        }
        launchContextualActionBar(selectionMode.size)
    }

    private fun enableOrDisableCreateCategory(createCategory: CreateCategory) {
        if (createCategory.isEnabled) binding.categoriesCreateitemview.setCreatingMode()
        else binding.categoriesCreateitemview.setIdleMode()
    }

    private fun setupCategoryList(categoryList: CategoryList) {
        if (!categoryList.mustRender) return

        if (binding.categoriesRecycler.adapter == null) {
            setupCategoryRecycler(categoryList.data)
        } else {
            val adapter = binding.categoriesRecycler.adapter as CategoryAdapter
            submitListAndScrollIfApplies(adapter, adapter.currentList, categoryList.data)
        }

        binding.categoriesFrameWizard.isVisible = categoryList.data.isEmpty()
    }

    private fun submitListAndScrollIfApplies(
        noteAdapter: CategoryAdapter,
        currentList: List<CategoryPresentationModel>,
        taskList: List<CategoryPresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        noteAdapter.submitList(taskList.toMutableList()) { if (isInsertingInList) scrollToTop() }
    }

    private fun setupCategoryRecycler(list: List<CategoryPresentationModel>) {
        binding.categoriesRecycler.apply {
            adapter = buildCategoryAdapter(this, list)
            layoutManager = buildVerticalManager(context).also { layoutManager ->
                //TODO
//                retrieveRecyclerStateIfApplies(layoutManager)
            }
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun buildCategoryAdapter(
        recyclerView: RecyclerView,
        list: List<CategoryPresentationModel>
    ): CategoryAdapter {
        return CategoryAdapter(
            onRename = { it, model -> onSaveRenaming(it, model) },
            recyclerView = recyclerView,
            onClick = { category -> onCategoryClicked(category) },
            onReorderSuccess = { categories -> onReorderedCategories(categories) },
            onReorderCanceled = { onReorderCanceled() },
            onSelection = { onSelection() }
        ).apply {
            submitList(list)
            setDragDirections(recyclerView, ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }
    }

    private fun onSaveRenaming(newName: String, presentationModel: CategoryPresentationModel) {
        viewModel.onSave(newName, presentationModel)
    }

    private fun onSelection() {
        viewModel.onSelection()
    }

    private fun onReorderCanceled() {
        viewModel.onReorderCategoriesCancelled()
    }

    private fun onReorderedCategories(categories: List<CategoryPresentationModel>) {
        viewModel.onReorderedCategories(categories)
    }

    private fun onCategoryClicked(category: CategoryPresentationModel) {
        //TODO
    }

    private fun scrollToTop() {
        binding.categoriesRecycler.scrollToPosition(0)
    }

    private fun launchContextualActionBar(sizeSelected: Int) {
        actionMode = setupContextualActionBar(
            toolbar = requireActivity().findViewById(R.id.toolbar),
            menuId = R.menu.menu_contextual_categories,
            currentActionMode = actionMode,
            adapter = binding.categoriesRecycler.adapter as CategoryAdapter,
            onActionClick = { onContextualActionItem(menuId = it) },
            onSetTitle = { selectedSize: Int ->
                getCategoriesSelectedTitle(
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
            onDeleteCategoriesConfirm()
            true
        }
        else -> false
    }

    private fun onDeleteCategoriesConfirm() {
        viewModel.onDeleteConfirmCategories()
    }


}