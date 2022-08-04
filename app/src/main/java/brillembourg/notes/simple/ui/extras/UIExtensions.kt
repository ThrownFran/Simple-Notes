package brillembourg.notes.simple.ui.extras

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

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