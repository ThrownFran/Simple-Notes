package brillembourg.notes.simple.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import brillembourg.notes.simple.databinding.ModalBottomSheetCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoriesViewModel
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.detail.setupSelectCategoriesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@AndroidEntryPoint
class SelectHomeFilterCategoriesModal : BottomSheetDialogFragment() {

    private val detailViewModel: HomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val categoryViewModel: CategoriesViewModel by viewModels()
    private lateinit var binding: ModalBottomSheetCategoriesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalBottomSheetCategoriesBinding.inflate(inflater, container, false)
        renderStates()
        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        binding.selectCategoriesImageClear.setOnClickListener { dismiss() }

        binding.selectCategoriesCreateitem.onReadyToCreateItem = { name ->
            categoryViewModel.onCreateCategory(name)
        }
    }

    private fun renderStates() {
        renderHomeSelectCategoryState()
        renderCategoryCreateState()
    }

    private fun renderCategoryCreateState() {
        safeUiLaunch {
            categoryViewModel.categoryUiState.collect {
                if (it.createCategory.isEnabled) {
                    binding.selectCategoriesCreateitem.setCreatingMode()
                } else {
                    binding.selectCategoriesCreateitem.setIdleMode()
                }
            }
        }
    }

    private fun renderHomeSelectCategoryState() {
        safeUiLaunch {
            combine(
                detailViewModel.allCategories,
                detailViewModel.homeUiState
            ) { a, b ->
                Pair(a, b)
            }.collectLatest {
                val allCategories = it.first
                val noteList = it.second.noteList
                val isShowing = it.second.selectCategoriesState.isShowing

                if (isShowing) {
                    setupSelectCategoriesAdapter(
                        binding.detailRecyclerCategories,
                        allCategories,
                        noteList.filteredCategories.map { categoryPresentationModel -> categoryPresentationModel.id }
                    ) { category, isChecked ->
                        onCheckedCategory(category, isChecked)
                    }
                } else {
                    dismiss()
                }
            }
        }
    }

    override fun onDestroy() {
        detailViewModel.onHideCategories()
        super.onDestroy()
    }


    private fun onCheckedCategory(
        category: CategoryPresentationModel,
        isChecked: Boolean
    ) {
        detailViewModel.onCategoryChecked(
            category,
            isChecked
        )
    }


    companion object {
        const val TAG = "SelectCategoryModalBottomSheet"
    }
}