package brillembourg.notes.simple.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import brillembourg.notes.simple.databinding.ModalBottomSheetCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoriesViewModel
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectNoteCategoriesModal : BottomSheetDialogFragment() {

    private val detailViewModel: DetailViewModel by viewModels(ownerProducer = { requireParentFragment() })
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
        safeUiLaunch {
            detailViewModel.uiDetailUiState.collect {
                if (it.selectCategories.isShowing) {
                    setupSelectCategoriesAdapter(
                        binding.detailRecyclerCategories,
                        it.selectCategories.categories,
                        it.noteCategories.map { it.id }
                    ) { category, isChecked ->
                        onCheckedCategory(category, isChecked)
                    }
                } else {
                    dismiss()
                }
            }
        }

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