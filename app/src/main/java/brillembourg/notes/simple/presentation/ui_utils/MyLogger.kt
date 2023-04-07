package brillembourg.notes.simple.presentation.ui_utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object MyLogger {

    fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
        Log.e("MyLogger", message)
    }

    fun record(e: Throwable) {
        e.message?.let {
            log(it)
        }
        FirebaseCrashlytics.getInstance().recordException(e)
        e.printStackTrace()
    }

}
