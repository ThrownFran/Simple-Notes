package brillembourg.notes.simple.ui.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ContentDelegate<R : AppCompatActivity, T : ViewDataBinding>(@LayoutRes val layoutRes: Int) :
    ReadOnlyProperty<R, T> {

    var binding: T? = null

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        if (binding == null) {
            binding = DataBindingUtil.setContentView<T>(thisRef, layoutRes).apply {
                lifecycleOwner = thisRef
            }
        }
        return binding!!
    }
}

fun <R : AppCompatActivity, T : ViewDataBinding>
        contentViews(@LayoutRes layoutRes: Int): ContentDelegate<R, T> =
    ContentDelegate(layoutRes)