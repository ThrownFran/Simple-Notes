package brillembourg.notes.simple.di

import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.data.DateProviderImp
import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.SchedulersImp
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.presentation.trash.MessageManagerImp
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
    fun dateProvider(): DateProvider = DateProviderImp()


    @Singleton
    @Provides
    fun dispatchers(): Schedulers {
        return SchedulersImp()
    }

    @Singleton
    @Provides
    fun messageManager(): MessageManager = MessageManagerImp()

}


