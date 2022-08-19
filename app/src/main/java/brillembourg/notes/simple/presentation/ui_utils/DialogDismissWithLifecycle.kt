package brillembourg.notes.simple.presentation.ui_utils

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/*Dialog that dismiss in onDestroy event, to avoid leaks*/
class DialogDismissWithLifecycle : LifecycleEventObserver {

    var dialog: AlertDialog? = null

    fun showWithLifecycle(
        builder: AlertDialog.Builder,
        lifecycleOwner: LifecycleOwner
    ): AlertDialog {
        lifecycleOwner.lifecycle.addObserver(this)
        this.dialog = builder.show()
        return this.dialog!!
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            dialog?.dismiss()
            dialog = null
        }
    }
}

fun AlertDialog.Builder.showWithLifecycle(lifecycleOwner: LifecycleOwner): AlertDialog {
    return DialogDismissWithLifecycle().showWithLifecycle(this, lifecycleOwner)
}