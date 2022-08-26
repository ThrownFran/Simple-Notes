package brillembourg.notes.simple.presentation.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox

class CustomCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatCheckBox(context, attrs, defStyleAttr) {
    private var listener: OnCheckedChangeListener? = null

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
        super.setOnCheckedChangeListener(listener)
    }

    fun setChecked(checked: Boolean, alsoNotify: Boolean) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null)
            super.setChecked(checked)
            super.setOnCheckedChangeListener(listener)
            return
        }
        super.setChecked(checked)
    }

    fun toggle(alsoNotify: Boolean) {
        if (!alsoNotify) {
            super.setOnCheckedChangeListener(null)
            super.toggle()
            super.setOnCheckedChangeListener(listener)
            return
        }
        super.toggle()
    }
}