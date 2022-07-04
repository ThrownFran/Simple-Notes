package brillembourg.notes.simple

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {

            androidContext(this@MyApp)

            module {
                listOf(appModule)
            }
        }
    }
}