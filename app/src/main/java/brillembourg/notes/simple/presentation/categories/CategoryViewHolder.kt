package brillembourg.notes.simple.presentation.categories

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.databinding.ItemCategoryBinding
import brillembourg.notes.simple.presentation.custom_views.fromDpToPixel
import brillembourg.notes.simple.presentation.custom_views.hideKeyboard
import brillembourg.notes.simple.presentation.custom_views.showSoftKeyboard
import brillembourg.notes.simple.presentation.ui_utils.Selectable
import brillembourg.notes.simple.presentation.ui_utils.SelectableImp

class CategoryViewHolder(
    private val onRename: (newName: String, CategoryPresentationModel) -> Unit,
    private val getCurrentList: () -> List<CategoryPresentationModel>,
    private val binding: ItemCategoryBinding,
    onClick: ((CategoryPresentationModel) -> Unit)? = null,
    private val onSelected: (() -> Unit)? = null,
    private val onReadyToDrag: ((CategoryViewHolder) -> Unit)? = null
) : RecyclerView.ViewHolder(binding.root),
    Selectable<CategoryPresentationModel> by SelectableImp(getCurrentList) {

    init {
        setupClickListeners()
        setupSelection(
            bindSelection = { categoryPresentationModel -> bindSelection(categoryPresentationModel) },
            onSelected = { _, _ -> onSelected?.invoke() },
            onClickWithNoSelection = { presentationModel ->

                editCategoryAndUnfocusOthers(presentationModel)

                onClick?.invoke(presentationModel)
            },
            onReadyToDrag = { onReadyToDrag?.invoke(this) }
        )
    }

    private fun editCategoryAndUnfocusOthers(presentationModel: CategoryPresentationModel) {
        unFocusAnyEditingCategory()
        editCategory(presentationModel)
    }

    private fun editCategory(presentationModel: CategoryPresentationModel) {
        presentationModel.isEditing = true
        bindingAdapter?.notifyItemChanged(bindingAdapterPosition, presentationModel)
    }

    private fun unFocusAnyEditingCategory() {
        getCurrentList().forEachIndexed { index, it ->
            if (it.isEditing) {
                it.isEditing = false
                bindingAdapter?.notifyItemChanged(index, it)
            }
        }
    }

    private fun setupClickListeners() {

        binding.categoryCardview.setOnClickListener {
            onItemClick(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
        }


        binding.categoryCardview.setOnLongClickListener {
            onItemSelection(bindingAdapterPosition, getCurrentList()[bindingAdapterPosition])
            true
        }

        binding.categoryImageEdit.setOnClickListener {
            onEdit()
        }

        binding.categoryImageSave.setOnClickListener {
            onSave()
        }

        binding.categoryImageCancel.setOnClickListener {
            onCancel()
        }
    }

    private fun onCancel() {
        val presentationModel = getCurrentList()[bindingAdapterPosition]
        unfocusCategory(presentationModel)
    }

    private fun onSave() {
        val presentationModel = getCurrentList()[bindingAdapterPosition]
        val newName = binding.categoryEditName.text.toString()

        if (newName != presentationModel.name) {
            renameCategory(newName, presentationModel)
        }

        unfocusCategory(presentationModel)
    }

    private fun onEdit() {
        editCategoryAndUnfocusOthers(getCurrentList()[bindingAdapterPosition])
    }

    private fun renameCategory(
        newName: String,
        presentationModel: CategoryPresentationModel
    ) {
        onRename.invoke(newName, presentationModel)
        presentationModel.name = newName
    }

    private fun unfocusCategory(presentationModel: CategoryPresentationModel) {
        presentationModel.isEditing = false
        bindingAdapter?.notifyItemChanged(bindingAdapterPosition, presentationModel)
    }

    fun bind(category: CategoryPresentationModel) {
        bindName(category)
        bindSelection(category)
        bindIsEditing(category)
    }

    private fun bindIsEditing(category: CategoryPresentationModel) {
        if (category.isEditing) {

            showEditingViews()

            binding.categoryEditName.apply {
                setText(category.name)
                requestFocus()
                showSoftKeyboard()
            }

        } else {
            hideEditingViews()

            binding.categoryEditName.apply {
                clearFocus()
            }

            hideKeyboardIfNoItemInListIsEditing()
        }
    }

    private fun hideKeyboardIfNoItemInListIsEditing() {
        if (getCurrentList().none { it.isEditing }) {
            binding.categoryEditName.hideKeyboard()
        }
    }

    private fun hideEditingViews() {
        binding.categoryImageLabel.isVisible = true
        binding.categoryImageSave.isVisible = false
        binding.categoryImageCancel.isVisible = false
        binding.categoryImageEdit.isVisible = true

        //Text fields
        binding.categoryEditName.isVisible = false
        binding.categoryTextName.isVisible = true
    }

    private fun showEditingViews() {
        binding.categoryTextName.isVisible = false
        binding.categoryImageLabel.isVisible = false
        binding.categoryImageSave.isVisible = true
        binding.categoryImageCancel.isVisible = true
        binding.categoryImageEdit.isVisible = false

        //Text fields
        binding.categoryEditName.isVisible = true
        binding.categoryTextName.isVisible = false
    }

    private fun bindName(category: CategoryPresentationModel) {
        binding.categoryTextName.setText(category.name)
    }

    private fun bindSelection(task: CategoryPresentationModel) {
        if (task.isSelected) {
            setBackgroundSelected()
        } else {
            setBackgroundTransparent()
        }
    }

    private fun setBackgroundTransparent() {
        binding.categoryCardview.isChecked = false
        binding.categoryCardview.strokeWidth =
            1f.fromDpToPixel(binding.categoryCardview.context).toInt()
    }

    private fun setBackgroundSelected() {
        binding.categoryCardview.isChecked = true
        binding.categoryCardview.strokeWidth =
            2.5f.fromDpToPixel(binding.categoryCardview.context).toInt()
    }


}