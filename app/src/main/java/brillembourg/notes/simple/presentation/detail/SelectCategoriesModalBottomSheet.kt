package brillembourg.notes.simple.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import brillembourg.notes.simple.databinding.ModalBottomSheetCategoriesBinding
import brillembourg.notes.simple.presentation.categories.CategoryPresentationModel
import brillembourg.notes.simple.presentation.custom_views.safeUiLaunch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectCategoriesModalBottomSheet : BottomSheetDialogFragment() {

    private val detailViewModel: DetailViewModel by viewModels(ownerProducer = { requireParentFragment() })
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
            detailViewModel.uiDetailUiState.collect {
                if (it.selectCategories.isShowing) {
                    setupCategories(it.selectCategories.categories, it.noteCategories)
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

    private fun setupCategories(
        allCategories: List<CategoryPresentationModel>,
        noteCategories: List<CategoryPresentationModel>
    ) {
        val recycler = binding.detailRecyclerCategories


        //copy to avoid modifying detailuistate
        val selectCategories = allCategories.toMutableList().map { it.copy() }

        //check categories from note
        selectCategories.forEach {
            if (noteCategories.contains(it)) {
                it.isSelected = true
            }
        }

        if (recycler.adapter == null) {
            recycler.apply {
                adapter = SelectCategoryAdapter { category, isChecked ->
                    onCheckedCategory(category, isChecked)
                }
                    .apply {
                        submitList(selectCategories)
                    }
                layoutManager = LinearLayoutManager(context)
            }
        } else {
            (recycler.adapter as SelectCategoryAdapter)
                .submitList(selectCategories)
        }
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