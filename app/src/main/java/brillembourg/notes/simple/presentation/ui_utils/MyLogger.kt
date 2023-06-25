package brillembourg.notes.simple.presentation.ui_utils

import android.os.Build
import android.util.Log
import brillembourg.notes.simple.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

object MyLogger {

    fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
        if(BuildConfig.DEBUG) {
            Log.e("MyLogger", message)
        }
    }

    fun record(e: Throwable) {
        e.message?.let {
            log(it)
        }
        FirebaseCrashlytics.getInstance().recordException(e)
        e.printStackTrace()
    }

}
