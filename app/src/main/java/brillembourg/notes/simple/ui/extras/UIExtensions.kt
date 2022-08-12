package brillembourg.notes.simple.ui.extras

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

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
    this.setBackgroundDrawable(this.themedContext.resources.getDrawable(resId))
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

fun Fragment.restartApp() {
    val packageManager = requireContext().packageManager
    val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

