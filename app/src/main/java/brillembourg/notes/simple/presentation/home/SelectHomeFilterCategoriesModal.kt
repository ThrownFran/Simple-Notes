package brillembourg.notes.simple.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import brillembourg.notes.simple.databinding.ModalBottomSheetCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import brillembourg.notes.simple.presentation.detail.setupSelectCategoriesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectHomeFilterCategoriesModal : BottomSheetDialogFragment() {

    private val detailViewModel: HomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
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
    }

    private fun renderStates() {
        safeUiLaunch {
            detailViewModel.homeUiState.collect {
                if (it.selectFilterCategories.isShowing) {
                    setupSelectCategoriesAdapter(
                        binding.detailRecyclerCategories,
                        it.selectFilterCategories.categories,
                        it.filteredCategories.map { categoryPresentationModel -> categoryPresentationModel.id }
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