package brillembourg.notes.simple.presentation.extras

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.use
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import brillembourg.notes.simple.presentation.base.MainActivity
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.asString
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

//fun Context.showToast(message: String) {
//    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//}

fun Context.showToast(message: UiText) {
    Toast.makeText(this, message.asString(this), Toast.LENGTH_LONG).show()
}

fun MainActivity.showMessage(message: String, onMessageShown: (() -> Unit)? = null) {
    Snackbar.make(binding.mainCoordinator, message, Snackbar.LENGTH_SHORT).apply {

        //Snackbar still not styleable in Material3 (Could not style text color)
        setTextColor(resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer))
        setBackgroundTint(resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer))

        addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onShown(transientBottomBar: Snackbar?) {
                super.onShown(transientBottomBar)
                binding.homeFab.shrink()
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                binding.homeFab.extend()
                onMessageShown?.invoke()
            }
        })

        show()
    }
}

/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun Context.resolveAttribute(@AttrRes attribute: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(
        attribute,
        typedValue,
        true
    )
    return typedValue.data
}

fun Fragment.showMessage(message: UiText, onMessageShown: (() -> Unit)? = null) {
    (activity as MainActivity).showMessage(message.asString(requireContext()), onMessageShown)
}

//fun Fragment.showMessage(message: String) {
//    (activity as MainActivity).showMessage(message)
//}

fun View.showSoftKeyboard() {
    if (requestFocus()) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Float.fromPixelToDp(context: Context): Float {
    return this / context.resources.displayMetrics.density
}

fun Float.fromDpToPixel(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun ActionBar.setBackgroundDrawable(@DrawableRes resId: Int) {
    val drawable = themedContext.resources.getDrawable(resId)
    this.setBackgroundDrawable(drawable)
}

fun View.hideKeyboard() = ViewCompat.getWindowInsetsController(this)
    ?.hide(WindowInsetsCompat.Type.ime())

fun ExtendedFloatingActionButton.animateWithRecycler(recyclerView: RecyclerView) {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0) {
                // Scroll Down
                if (isExtended) {
                    shrink()
                }
            } else if (dy < 0) {
                // Scroll Up
                if (!isExtended) {
                    extend()
                }
            }
        }
    })
}

fun Toolbar.lockScroll() {
    val params: AppBarLayout.LayoutParams = this.layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
}

fun Toolbar.unLockScroll() {
    val params: AppBarLayout.LayoutParams = this.layoutParams as AppBarLayout.LayoutParams
    params.scrollFlags =
        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
}

fun Activity.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

