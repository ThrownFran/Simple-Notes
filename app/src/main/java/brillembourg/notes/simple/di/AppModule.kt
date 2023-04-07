package brillembourg.notes.simple.di

import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.data.DateProviderImp
import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.SchedulersImp
import brillembourg.notes.simple.domain.use_cases.theme.ThemeManager
import brillembourg.notes.simple.presentation.ThemeManagerImp
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.base.MessageManagerImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun dispatchers(): Schedulers {
        return SchedulersImp()
    }

    @Singleton
    @Provides
    fun messageManager(): MessageManager = MessageManagerImp()


    @Singleton
    @Provides
    fun dateProvider(): DateProvider = DateProviderImp()

    @Singleton
    @Provides
    fun getThemeManager(): ThemeManager {
        return ThemeManagerImp()
    }

}


