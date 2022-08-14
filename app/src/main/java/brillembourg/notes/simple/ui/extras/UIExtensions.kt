package brillembourg.notes.simple.ui.extras

import android.annotation.SuppressLint
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
import brillembourg.notes.simple.ui.base.MainActivity
import brillembourg.notes.simple.ui.detail.DetailFragment
import brillembourg.notes.simple.ui.home.HomeFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun MainActivity.showMessage(message: String) {
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

fun HomeFragment.showMessage(message: String) {
    (activity as MainActivity).showMessage(message)
}

fun DetailFragment.showMessage(message: String) {
    (activity as MainActivity).showMessage(message)
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
    val drawable = themedContext.resources.getDrawable(resId)
//    val color = themedContext.resolveAttribute(com.google.android.material.R.attr.colorPrimary)
//    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
//    DrawableCompat.setTint(drawable, themedContext.resolveAttribute(com.google.android.material.R.attr.colorPrimary));

//    val backgroundShapeModel: ShapeAppearanceModel = ShapeAppearanceModel.builder()
//        .setTopLeftCorner(CornerFamily.ROUNDED, 16F.toPx)
//        .setTopRightCorner(CornerFamily.ROUNDED, 16F.toPx)
//        .build()
//    this.setBackgroundDrawable(MaterialShapeDrawable(backgroundShapeModel).apply {
//        fillColor = ColorStateList.valueOf(Color.GREEN)
//    })


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

fun Fragment.restartApp() {
    val packageManager = requireContext().packageManager
    val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

