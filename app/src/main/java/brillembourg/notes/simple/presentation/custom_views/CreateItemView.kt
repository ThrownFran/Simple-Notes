package brillembourg.notes.simple.presentation.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.LayoutCreateCategoryBinding

class CreateItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), LifecycleOwner {

    private val binding =
        LayoutCreateCategoryBinding.inflate(LayoutInflater.from(context), this, true)
    private val layoutAttributes = context.obtainStyledAttributes(attrs, R.styleable.CreateItemView)
    private val nameAttr: String? =
        layoutAttributes.getString(R.styleable.CreateItemView_createItemText)
    private val lifeCycleRegistry = LifecycleRegistry(this)

    private var editingText: String = ""
    private var isCreationEnabled = false

    var onReadyToCreateItem: ((String) -> Unit)? = null

    override val lifecycle: Lifecycle
        get() = lifeCycleRegistry

    private fun onUpdateState() {
        val isCreatingCategory = isCreationEnabled
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

    override fun onAttachedToWindow() {
        lifeCycleRegistry.currentState = Lifecycle.State.STARTED
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        lifeCycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }

    init {
        nameAttr?.let {
            binding.categoriesEditAdd.hint = it
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesImageAddStart.onClickFlow.collect {
                onShowCreateCategory()
            }
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesImageAddClear.onClickFlow.collect {
                onShowCreateCategoryDismiss()
            }
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesImageAddSuccess.onClickFlow.collect {
                onCreateCategory {
                    onReadyToCreateItem?.invoke(editingText)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesEditAdd.onClickFlow.collect {
                if (!it.isFocused) onShowCreateCategory()
            }
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesEditAdd.onFocusFlow.collect {
                if (it) onShowCreateCategory()
            }
        }

        lifecycleScope.launchWhenStarted {
            binding.categoriesEditAdd.onTextChangedFlow.collect {
                editingText = it
            }
        }
    }

    private fun onShowCreateCategory() {
        isCreationEnabled = true
        onUpdateState()
    }

    private fun onCreateCategory(onCreate: (String) -> Unit) {
        val name = editingText

        if (name.isEmpty()) {
            isCreationEnabled = false
            editingText = ""
            onUpdateState()
            return
        }

        onCreate.invoke(name)
        isCreationEnabled = false
        editingText = ""
        onUpdateState()
    }

    private fun onShowCreateCategoryDismiss() {
        isCreationEnabled = false
        editingText = ""
        onUpdateState()
    }

    fun setCreatingMode() {
        isCreationEnabled = true
        onUpdateState()
    }

    fun setIdleMode() {
        isCreationEnabled = false
        onUpdateState()
    }


}

