package brillembourg.notes.simple.presentation.ui_utils

import android.util.Log

object MyLogger {

    fun log(message: String) {
        //TODO Crashlytics
//        FirebaseCrashlytics.getInstance().log(message)
        Log.e("MyLogger", message)
    }

    fun record(e: Throwable) {
        //TODO Crashlytics
        e.message?.let {
            log(it)
        }
//        FirebaseCrashlytics.getInstance().recordException(e)
        e.printStackTrace()
    }

}
