package brillembourg.notes.simple.presentation.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import brillembourg.notes.simple.databinding.FragmentCategoriesBinding
import brillembourg.notes.simple.presentation.custom_views.hideKeyboard
import brillembourg.notes.simple.presentation.custom_views.onClickFlow
import brillembourg.notes.simple.presentation.custom_views.onFocusFlow
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.ui_utils.recycler_view.buildVerticalManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    companion object {
        fun newInstance() = CategoriesFragment()
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var _binding: FragmentCategoriesBinding? = null
    private lateinit var binding: FragmentCategoriesBinding

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
        safeUiLaunch {
            binding.categoriesImageAddStart.onClickFlow.collect {
                viewModel.onShowCreateCategory()
            }
        }

        safeUiLaunch {
            binding.categoriesImageAddClear.onClickFlow.collect {
                viewModel.onShowCreateCategoryDismiss()
            }
        }

        safeUiLaunch {
            binding.categoriesImageAddSuccess.onClickFlow.collect {
                viewModel.onCreateCategory()
            }
        }

        safeUiLaunch {
            binding.categoriesEditAdd.onClickFlow.collect {
                if (!it.isFocused) viewModel.onShowCreateCategory()
            }
        }

        safeUiLaunch {
            binding.categoriesEditAdd.onFocusFlow.collect {
                if (it) viewModel.onShowCreateCategory()
            }
        }
    }

    private fun renderStates() {
        safeUiLaunch {
            viewModel.categoryUiState.collect {

                setupCategoryList(it.list)

                enableOrDisableCreateCategory(it.createCategory)
            }
        }
    }

    private fun enableOrDisableCreateCategory(createCategory: CreateCategory) {

        val isCreatingCategory = createCategory.isEnabled
        binding.categoriesImageAddStart.visibility =
            if (isCreatingCategory) View.INVISIBLE else View.VISIBLE
        binding.categoriesImageAddSuccess.isVisible = isCreatingCategory
        binding.categoriesImageAddClear.isVisible = isCreatingCategory

        binding.categoriesEditAdd.apply {
            if (isCreatingCategory) {
                requestFocus()
            } else {
                setText("")
                clearFocus()
                binding.categoriesEditAdd.hideKeyboard()
            }
        }
    }

    private fun setupCategoryList(list: List<CategoryPresentationModel>) {
        if (binding.categoriesRecycler.adapter == null) {
            setupCategoryRecycler(list)
        } else {
            val adapter = binding.categoriesRecycler.adapter as CategoryAdapter
            submitListAndScrollIfApplies(adapter, adapter.currentList, list)
        }
    }

    private fun submitListAndScrollIfApplies(
        noteAdapter: CategoryAdapter,
        currentList: List<CategoryPresentationModel>,
        taskList: List<CategoryPresentationModel>
    ) {
        val isInsertingInList = currentList.size < taskList.size
        noteAdapter.submitList(taskList) { if (isInsertingInList) scrollToTop() }
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
            recyclerView = recyclerView,
            onClick = { category -> onCategoryClicked(category) },
            onReorderSuccess = { categories -> onReorderedCategories(categories) },
            onReorderCanceled = { onReorderCanceled() },
            onSelection = { onSelection() }
        ).apply {
            submitList(list)
        }
    }

    private fun onSelection() {
        //TODO
    }

    private fun onReorderCanceled() {
        //TODO
    }

    private fun onReorderedCategories(categories: List<CategoryPresentationModel>) {
        //TODO
    }

    private fun onCategoryClicked(category: CategoryPresentationModel) {
        //TODO
    }

    private fun scrollToTop() {
        binding.categoriesRecycler.scrollToPosition(0)
    }


}